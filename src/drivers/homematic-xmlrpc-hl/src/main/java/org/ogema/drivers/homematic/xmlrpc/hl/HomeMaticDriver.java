/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3
 * as published by the Free Software Foundation.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ogema.drivers.homematic.xmlrpc.hl;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.xmlrpc.XmlRpcException;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.application.Timer;
import org.ogema.core.application.TimerListener;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.resourcemanager.ResourceDemandListener;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.drivers.homematic.xmlrpc.hl.channels.ChannelHandler;
import org.ogema.drivers.homematic.xmlrpc.hl.channels.KeyChannel;
import org.ogema.drivers.homematic.xmlrpc.hl.channels.MaintenanceChannel;
import org.ogema.drivers.homematic.xmlrpc.hl.channels.MotionDetectorChannel;
import org.ogema.drivers.homematic.xmlrpc.hl.channels.PowerMeterChannel;
import org.ogema.drivers.homematic.xmlrpc.hl.channels.ShutterContactChannel;
import org.ogema.drivers.homematic.xmlrpc.hl.channels.SwitchChannel;
import org.ogema.drivers.homematic.xmlrpc.hl.channels.ThermostatChannel;
import org.ogema.drivers.homematic.xmlrpc.hl.channels.WeatherChannel;
import org.ogema.drivers.homematic.xmlrpc.hl.types.HmDevice;
import org.ogema.drivers.homematic.xmlrpc.hl.types.HmLogicInterface;
import org.ogema.drivers.homematic.xmlrpc.ll.HomeMaticClient;
import org.ogema.drivers.homematic.xmlrpc.ll.HomeMaticClientCli;
import org.ogema.drivers.homematic.xmlrpc.ll.HomeMaticService;
import org.ogema.drivers.homematic.xmlrpc.ll.api.DeviceDescription;
import org.ogema.drivers.homematic.xmlrpc.ll.api.HmEvent;
import org.ogema.drivers.homematic.xmlrpc.ll.api.HomeMatic;
import org.ogema.drivers.homematic.xmlrpc.ll.api.ParameterDescription;
import org.ogema.model.actors.OnOffSwitch;
import org.ogema.model.connections.ElectricityConnection;
import org.ogema.model.devices.sensoractordevices.SingleSwitchBox;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;

/**
 *
 * @author jlapp
 */
@Component(service = Application.class)
public class HomeMaticDriver implements Application {
    
    // aggregate channels of known homematic device types into higher level ogema devices?
    // XXX breaks intended usage pattern of used channels, move to separate bundle?
    private boolean performHighLevelDeviceSetup = true;
    // create aggregate devices on top level?
    private boolean highLevelDevicesTopLevel = false;

    private ApplicationManager appman;
    private ComponentContext ctx;
    
    HomeMaticService hm;
    HomeMatic client;
    private HmLogicInterface baseResource;
    private Persistence persistence;
    private Logger logger;
    
    /* Chain of responsibility for handling HomeMatic devices, the first
    handler that accepts a DeviceDescription will control that device 
    (=> register more specific handlers first; not actually useful for standard
    homematic device types) */
    private final List<ChannelHandler> handlers = new CopyOnWriteArrayList<>();

    @Activate
    protected void activate(ComponentContext ctx) {
        System.out.println("activated;");
        this.ctx = ctx;
    }

    final ResourceDemandListener<HmDevice> devResourceListener = new ResourceDemandListener<HmDevice>() {

        @Override
        public void resourceAvailable(HmDevice t) {
            setupDevice(t);
        }

        @Override
        public void resourceUnavailable(HmDevice t) {
            //TODO
        }

    };

    final ResourceDemandListener<HmLogicInterface> configListener = new ResourceDemandListener<HmLogicInterface>() {

        @Override
        public void resourceAvailable(HmLogicInterface t) {
            init(t);
        }

        @Override
        public void resourceUnavailable(HmLogicInterface t) {
            //TODO
        }

    };
    
    final ResourceValueListener<BooleanResource> installModeListener = new ResourceValueListener<BooleanResource> () {

        @Override
        public void resourceChanged(BooleanResource t) {
            if (t.equals(baseResource.installationMode().stateControl())) {
                try {
                    boolean onOff = t.getValue();
                    client.setInstallMode(onOff, 900, 1);
                    int secondsRemaining = client.getInstallMode();
                    baseResource.installationMode().stateFeedback().setValue(secondsRemaining > 0);
                } catch (XmlRpcException ex) {
                    logger.error("could not activate install mode", ex);
                }
            } else if (t.equals(baseResource.installationMode().stateFeedback())) {
                boolean installModeOn = baseResource.installationMode().stateFeedback().getValue();
                logger.info("installation mode {}", installModeOn? "on" : "off");
            }
        }
        
    };
    
    final TimerListener installModePolling = new TimerListener() {

        @Override
        public void timerElapsed(Timer timer) {
            try {
                int secondsRemaining = client.getInstallMode();
                logger.debug("polled installation mode: {}s", secondsRemaining);
                baseResource.installationMode().stateFeedback().setValue(secondsRemaining > 0);
            } catch (XmlRpcException ex) {
                logger.error("could not poll HomeMatic client for installation mode state", ex);
            }
        }
        
    };

    @Override
    public void start(ApplicationManager am) {
        appman = am;
        logger = am.getLogger();

        handlers.add(new MaintenanceChannel());
        handlers.add(new ThermostatChannel());
        handlers.add(new SwitchChannel());
        handlers.add(new PowerMeterChannel());
        handlers.add(new WeatherChannel());
        handlers.add(new ShutterContactChannel());
        handlers.add(new MotionDetectorChannel());
        handlers.add(new KeyChannel());

        logger.info("HomeMatic driver ready, configuration pending");
        am.getResourceAccess().addResourceDemand(HmLogicInterface.class, configListener);
    }

    protected void pollParameters() {
        for (HmDevice dev : baseResource.devices().getAllElements()) {
            for (HmDevice sub : dev.channels().getAllElements()) {
                setupDevice(sub);
            }
        }
    }

    protected void init(HmLogicInterface config) {
        try {
            String serverUrl = null;
            String urlPattern = config.baseUrl().getValue();
            if (urlPattern == null || urlPattern.isEmpty()) {
                urlPattern = "http://%s:%d";
            }
            String alias = config.alias().getValue();
            String iface = config.networkInterface().getValue();
            if (iface != null && !iface.isEmpty()) {
                Enumeration<InetAddress> addresses = NetworkInterface.getByName(iface).getInetAddresses();
                String ipAddrString = null;
                while (addresses.hasMoreElements()) {
                    InetAddress a = addresses.nextElement();
                    if (a instanceof Inet4Address) {
                        ipAddrString = a.getHostAddress();
                    }
                    serverUrl = String.format(urlPattern, ipAddrString, config.port().getValue());
                }
                if (ipAddrString == null) {
                    throw new IllegalArgumentException("could not determine IP address for interface " + iface);
                }
            } else {
                serverUrl = urlPattern + alias;
            }

            client = new HomeMaticClient(config.clientUrl().getValue());
            new HomeMaticClientCli(client).register(ctx.getBundleContext());

            hm = new HomeMaticService(ctx.getBundleContext(), serverUrl, alias);

            baseResource = config;
            baseResource.installationMode().stateControl().create();
            baseResource.installationMode().stateFeedback().create();
            baseResource.installationMode().activate(true);
            baseResource.installationMode().stateControl().addValueListener(installModeListener, true);
            baseResource.installationMode().stateFeedback().addValueListener(installModeListener, false);
            persistence = new Persistence(appman, baseResource);
            hm.setBackend(persistence);

            hm.addDeviceListener(persistence);
            hm.init(client, "ogema");
            
            appman.getResourceAccess().addResourceDemand(HmDevice.class, devResourceListener);
            
            appman.createTimer(30000, installModePolling);
            
            logger.info("HomeMatic driver configured and registered according to config {}", config.getPath());
        } catch (IOException | XmlRpcException ex) {
            logger.error("could not start HomeMatic driver");
            logger.debug("Exception details:", ex);
            //throw new IllegalStateException(ex);
        }

    }

    protected void setupDevice(HmDevice dev) {
        try {
            String address = dev.address().getValue();
            DeviceDescription channelDesc = client.getDeviceDescription(address);
            for (ChannelHandler h : handlers) {
                if (h.accept(channelDesc)) {
                    logger.debug("handler available for {}: {}", address, h.getClass().getCanonicalName());
                    Map<String, Map<String, ParameterDescription<?>>> paramSets = new HashMap<>();
                    for (String set : dev.paramsets().getValues()) {
                        logger.trace("requesting paramset {} of device {}", set, address);
                        paramSets.put(set, client.getParamsetDescription(address, set));
                    }

                    h.setup(dev.getParent().getParent(), this, channelDesc, paramSets);
                    performHighLevelDeviceSetup(dev.getParent().getParent());
                    break;
                }
            }
        } catch (XmlRpcException ex) {
            logger.error("failed to configure value resources for device " + dev.getPath(), ex);
        }
    }

    @Override
    public void stop(AppStopReason asr) {

    }

    public void performSetValue(String address, String valueKey, Object value) {
        try {
            client.setValue(address, valueKey, value);
        } catch (XmlRpcException ex) {
            logger.error(String.format("setValue failed for %s@%s := %s", valueKey, address, value), ex);
        }
    }

    public static String sanitizeResourcename(String name) {
        return name.replaceAll("[^\\p{javaJavaIdentifierPart}]", "_");
    }

    public void storeEvent(HmEvent e, SingleValueResource res) {
        logger.debug("storing event data for {}@{} to {}", e.getValueKey(), e.getAddress(), res.getPath());
        if (res instanceof FloatResource) {
            ((FloatResource) res).setValue(e.getValueFloat());
        } else if (res instanceof IntegerResource) {
            ((IntegerResource) res).setValue(e.getValueInt());
        } else if (res instanceof StringResource) {
            ((StringResource) res).setValue(e.getValueString());
        } else if (res instanceof BooleanResource) {
            ((BooleanResource) res).setValue(e.getValueBoolean());
        } else {
            logger.warn("HomeMatic parameter resource is of unsupported type: {}", res.getResourceType());
        }
    }

    public HomeMaticService getHomeMaticService() {
        return hm;
    }
    
    /*
     * called after each channel setup to perform high-level setup of OGEMA device types,
     * e.g. by aggregating channels into new sub resources (SwitchChannel+PowerMeterChannel = SingleSwitchBox)
     */
    protected void performHighLevelDeviceSetup(HmDevice dev) {
        if (!performHighLevelDeviceSetup) {
            return;
        }
        //logger.debug("perform high level setup for device {}", dev.address().getValue());
        List<ElectricityConnection> elConns = dev.getSubResources(ElectricityConnection.class, false);
        List<OnOffSwitch> switches = dev.getSubResources(OnOffSwitch.class, false);
        if ("HM-ES-PMSw1-Pl".equals(dev.type().getValue()) && elConns.size() == 1 && switches.size() == 1){
            String ssbName = sanitizeResourcename("HM-SingleSwitchBox-" + dev.address().getValue());
            if ((highLevelDevicesTopLevel && appman.getResourceAccess().getResource(ssbName) != null) || dev.getSubResource(ssbName) != null) {
                return;
            }
            logger.debug("set up SingleSwitchBox for HomeMatic device {}", dev.address().getValue());
            OnOffSwitch sw = switches.get(0);
            ElectricityConnection elConn = elConns.get(0);

            SingleSwitchBox ssb = highLevelDevicesTopLevel? appman.getResourceManagement().createResource(ssbName, SingleSwitchBox.class) :
                    dev.getSubResource(ssbName, SingleSwitchBox.class);
            
            ssb.onOffSwitch().stateControl().create().activate(false);
            ssb.onOffSwitch().stateFeedback().create().activate(false);
            ssb.electricityConnection().create().activate(false);
            ssb.onOffSwitch().activate(false);
            elConn.setAsReference(ssb.electricityConnection());
            sw.setAsReference(ssb.onOffSwitch());

            ssb.activate(false);
        }
    }

}
