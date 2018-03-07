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
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.xmlrpc.XmlRpcException;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.application.Timer;
import org.ogema.core.application.TimerListener;
import org.ogema.core.model.Resource;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.resourcemanager.ResourceDemandListener;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.drivers.homematic.xmlrpc.hl.channels.KeyChannel;
import org.ogema.drivers.homematic.xmlrpc.hl.channels.MaintenanceChannel;
import org.ogema.drivers.homematic.xmlrpc.hl.channels.MotionDetectorChannel;
import org.ogema.drivers.homematic.xmlrpc.hl.channels.PMSwitchDevice;
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
import org.ogema.drivers.homematic.xmlrpc.ll.xmlrpc.MapXmlRpcStruct;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ogema.drivers.homematic.xmlrpc.hl.api.DeviceHandler;
import org.ogema.drivers.homematic.xmlrpc.hl.api.DeviceHandlerFactory;
import org.ogema.drivers.homematic.xmlrpc.ll.api.HmEventListener;
import org.ogema.drivers.homematic.xmlrpc.hl.api.HomeMaticConnection;

/**
 *
 * @author jlapp
 */
@Component(service = Application.class)
public class HomeMaticDriver implements Application {

    private ApplicationManager appman;
    private ComponentContext ctx;
    private Logger logger = LoggerFactory.getLogger(getClass());
    private final Map<HmLogicInterface, HmConnection> connections = new HashMap<>();

    private final SortedSet<HandlerRegistration> handlerFactories = new TreeSet<>();
    
    // store accepted devices (by address) so they are not offered again on a different connection
    private final Map<String, Class<? extends DeviceHandler>> acceptedDevices = new HashMap<>();

    private static class HandlerRegistration implements Comparable<HandlerRegistration> {

        DeviceHandlerFactory fac;
        int ranking;

        public HandlerRegistration(DeviceHandlerFactory fac, int ranking) {
            this.fac = fac;
            this.ranking = ranking;
        }

        @Override
        public int compareTo(HandlerRegistration o) {
            int rankCompare = Integer.compare(ranking, o.ranking);
            return rankCompare == 0 ? o.fac.getClass().getCanonicalName().compareTo(fac.getClass().getCanonicalName())
                    : - rankCompare;
        }

        @Override
        public boolean equals(Object obj) {
            return (obj instanceof HandlerRegistration) && fac.getClass() == ((HandlerRegistration) obj).fac.getClass();
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(fac);
        }

        @Override
        public String toString() {
            return String.format("%d: %s", ranking, fac.getClass().getCanonicalName());
        }
        
    }
    
    // this is a static + greedy reference so that external handler factories
    // can always replace the built-in handlers.
    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policyOption = ReferencePolicyOption.GREEDY)
    protected void addHandlerFactory(DeviceHandlerFactory fac, Map<String, Object> serviceProperties) {
        int ranking = 1;
        if (serviceProperties.containsKey(Constants.SERVICE_RANKING)) {
            ranking = (int) serviceProperties.get(Constants.SERVICE_RANKING);
        }
        logger.info("adding handler factory {}, rank {}", fac, ranking);
        synchronized (handlerFactories) {
            handlerFactories.add(new HandlerRegistration(fac, ranking));
        }
    }
    
    protected void removeHandlerFactory(DeviceHandlerFactory fac, Map<String, Object> serviceProperties) {
        // nothing to do for static reference handling
    }

    public class HmConnection implements HomeMaticConnection {

    	Thread initThread;
        HomeMaticService hm;
        HomeMatic client;
        HmLogicInterface baseResource;
        ServiceRegistration<HomeMaticClientCli> commandLine;
        // FIXME better use Java time than OGEMA timer in hardware driver?
        volatile Timer t;
        /* Chain of responsibility for handling HomeMatic devices, the first
         handler that accepts a DeviceDescription will control that device 
         (=> register more specific handlers first; not actually useful for standard
         homematic device types) */
        private final List<DeviceHandler> handlers;
        
        HmConnection(List<DeviceHandlerFactory> handlerFactories) {
            this.handlers = new ArrayList<>();
            for (DeviceHandlerFactory fac: handlerFactories) {
                this.handlers.add(fac.createHandler(this));
            }
            this.handlers.add(new PMSwitchDevice(this));
            this.handlers.add(new MaintenanceChannel(this));
            this.handlers.add(new ThermostatChannel(this));
            this.handlers.add(new SwitchChannel(this));
            this.handlers.add(new PowerMeterChannel(this));
            this.handlers.add(new WeatherChannel(this));
            this.handlers.add(new ShutterContactChannel(this));
            this.handlers.add(new MotionDetectorChannel(this));
            this.handlers.add(new KeyChannel(this));
        }
        
        @Override
        public void addEventListener(HmEventListener l) {
            hm.addEventListener(l);
        }
        
        @Override
        public void removeEventListener(HmEventListener l) {
            hm.removeEventListener(l);
        }

        final ResourceValueListener<BooleanResource> installModeListener = new ResourceValueListener<BooleanResource>() {

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
                    logger.info("installation mode {}", installModeOn ? "on" : "off");
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
        public void performSetValue(String address, String valueKey, Object value) {
            try {
                client.setValue(address, valueKey, value);
            } catch (XmlRpcException ex) {
                logger.error(String.format("setValue failed for %s@%s := %s", valueKey, address, value), ex);
            }
        }

        @Override
        public void performPutParamset(String address, String set, Map<String, Object> values) {
            try {
                MapXmlRpcStruct valueStruct = new MapXmlRpcStruct(values);
                client.putParamset(address, set, valueStruct);
            } catch (XmlRpcException ex) {
                logger.error(String.format("putParamset failed for %s@%s := %s", set, address, values), ex);
            }
        }

        @Override
        public void performAddLink(String sender, String receiver, String name, String description) {
            try {
                client.addLink(sender, receiver, name, description);
                logger.debug("added HomeMatic link: {} => {}", sender, receiver);
            } catch (XmlRpcException ex) {
                logger.error(String.format("addLink failed for {} => {}", sender, receiver), ex);
            }
        }

        @Override
        public void performRemoveLink(String sender, String receiver) {
            try {
                client.removeLink(sender, receiver);
                logger.debug("removed HomeMatic link: {} => {}", sender, receiver);
            } catch (XmlRpcException ex) {
                logger.error(String.format("removeLink failed for {} => {}", sender, receiver), ex);
            }
        }

        @Override
        public List<Map<String, Object>> performGetLinks(String address, int flags) {
            try {
                logger.debug("get links for {}", address);
                return client.getLinks(address, flags);
            } catch (XmlRpcException ex) {
                logger.error(String.format("getLinks failed for {}", address), ex);
                return null;
            }
        }
        
        /**
         * Returns the HmDevice element controlling the given OGEMA resource, or
         * null if the resource is not controlled by the HomeMatic driver.
         *
         * @param ogemaDevice
         * @return HomeMatic device resource controlling the given resource or
         * null.
         */
        @SuppressWarnings("rawtypes")
        @Override
        public HmDevice findControllingDevice(Resource ogemaDevice) {
            //XXX: review this mess
            for (ResourceList l : ogemaDevice.getReferencingResources(ResourceList.class)) {
                if (l.getParent() != null && l.getParent() instanceof HmDevice) {
                    return l.getParent();
                }
            }
            for (Resource ref : ogemaDevice.getLocationResource().getReferencingNodes(true)) {
                if (ref.getParent() != null && ref.getParent().getParent() instanceof HmDevice) {
                    return ref.getParent().getParent();
                }
                for (ResourceList l : ref.getReferencingResources(ResourceList.class)) {
                    if (l.getParent() != null && l.getParent() instanceof HmDevice) {
                        return l.getParent();
                    }
                }
            }
            return null;
        }

        @Override
        public HmDevice getToplevelDevice(HmDevice channel) {
            if (channel.getParent() != null && channel.getParent().getParent() instanceof HmDevice) {
                return channel.getParent().getParent();
            } else {
                return channel;
            }
        }

        @Override
        public HmDevice getChannel(HmDevice device, String channelAddress) {
            Objects.requireNonNull(device);
            Objects.requireNonNull(channelAddress);
            for (HmDevice channel : device.channels().getAllElements()) {
                if (channelAddress.equalsIgnoreCase(channel.address().getValue())) {
                    return channel;
                }
            }
            return null;
        }

        @Override
        public void registerControlledResource(HmDevice channel, Resource ogemaDevice) {
            Objects.requireNonNull(channel);
            Objects.requireNonNull(ogemaDevice);
            for (Resource entry : channel.controlledResources().getAllElements()) {
                if (entry.equalsLocation(ogemaDevice)) {
                    return;
                }
            }
            channel.controlledResources().create().activate(false);
            channel.controlledResources().add(ogemaDevice);
        }
        
    }

    @Activate
    protected void activate(ComponentContext ctx) {
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
            List<DeviceHandlerFactory> l = new ArrayList<>(handlerFactories.size());
            synchronized (handlerFactories) {
                for (HandlerRegistration reg: handlerFactories) {
                    l.add(reg.fac);
                }
            }
            HmConnection conn = new HmConnection(l);
            conn.baseResource = t;
            connections.put(t, conn);
            init(conn);
        }

        @Override
        public void resourceUnavailable(HmLogicInterface t) {
            close(connections.remove(t));
        }

    };

    @Override
    public void start(ApplicationManager am) {
        appman = am;
        logger = am.getLogger();
        // delay actual setup so that all external ChannelHandlerFactories
        // are available (cosmetic change, quick restarts seem to work fine)
        final Timer t = appman.createTimer(2000);
        t.addListener(new TimerListener() {
            @Override
            public void timerElapsed(Timer timer) {
                logger.info("HomeMatic driver ready, configuration pending");
                appman.getResourceAccess().addResourceDemand(HmLogicInterface.class, configListener);
                t.destroy();
            }
        });
    }

    @Override
    public void stop(AppStopReason asr) {
        if (appman != null) {
            appman.getResourceAccess().removeResourceDemand(HmLogicInterface.class, configListener);
        }
    	final Iterator<HmConnection> it = connections.values().iterator();
    	while (it.hasNext()) {
    		final HmConnection conn = it.next();
    		it.remove();
    		close(conn);
    	}
    }

    protected void pollParameters(HmConnection connection) {
        for (HmDevice dev : connection.baseResource.devices().getAllElements()) {
            for (HmDevice sub : dev.channels().getAllElements()) {
                setupDevice(sub);
            }
        }
    }

    protected void init(final HmConnection connection) {
        try {
            String serverUrl = null;
            final HmLogicInterface config = connection.baseResource;
            String urlPattern = config.baseUrl().getValue();
            // FIXME double use of the baseUrl resource is somewhat risky... is it really intended that one can either specify
            // an URL (if networkInterface is not set) or a pattern for the address (if networkInterface is also set)?
            if (urlPattern == null || urlPattern.isEmpty()) {
                urlPattern = "http://%s:%d";
            }
            String alias = config.alias().getValue();
            String iface = config.networkInterface().getValue();
            final boolean ifSet = config.networkInterface().isActive() || config.baseUrl().isActive();
            NetworkInterface n = null;
            if (!ifSet) {
                n = getBestMatchingInterface(config.clientUrl().getValue());
                if (n == null) {
                    throw new IllegalArgumentException(
                            "Bad configuration: Network interface or base url not set and failed to determine interface automatically");
                } else {
                    logger.debug("network interface selected for {}: {}", config.clientUrl().getValue(), n);
                }
            }
            if (!ifSet || (iface != null && !iface.isEmpty())) {
                Enumeration<InetAddress> addresses;
                if (n != null) {
                    addresses = n.getInetAddresses();
                } else {
                    NetworkInterface nif = NetworkInterface.getByName(iface);
                    if (nif == null) {
                        throw new IllegalStateException("no such network interface: " + iface);
                    }
                    addresses = nif.getInetAddresses();
                }
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

            connection.client = new HomeMaticClient(config.clientUrl().getValue());
            connection.commandLine = new HomeMaticClientCli(connection.client).register(ctx.getBundleContext(), config.getName());

            connection.hm = new HomeMaticService(ctx.getBundleContext(), serverUrl, alias);

            config.installationMode().stateControl().create();
            config.installationMode().stateFeedback().create();
            config.installationMode().activate(true);
            config.installationMode().stateControl().addValueListener(connection.installModeListener, true);
            config.installationMode().stateFeedback().addValueListener(connection.installModeListener, false);
            Persistence persistence = new Persistence(appman, config);
            connection.hm.setBackend(persistence);

            connection.hm.addDeviceListener(persistence);
            connection.initThread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					try { // blocks for ca. 20s if no connection can be established
						connection.hm.init(connection.client, "ogema-"+connection.baseResource.getName());
					} catch (XmlRpcException e) {
						if (Thread.interrupted()) {
							Thread.currentThread().interrupt();
							return;
						}
						logger.error("could not start HomeMatic driver for config {}", connection.baseResource.getPath());
			            logger.debug("Exception details:", e);
			            return;
					} 
					if (Thread.interrupted()) {
						Thread.currentThread().interrupt();
						return;
					}
		            appman.getResourceAccess().addResourceDemand(HmDevice.class, devResourceListener);
		            connection.t = appman.createTimer(30000, connection.installModePolling);
		            logger.info("HomeMatic driver configured and registered according to config {}", config.getPath());
				}
			}, "homematic-xmlrpc-init");
            connection.initThread.start();
        } catch (IOException ex) {
            logger.error("could not start HomeMatic driver for config {}", connection.baseResource.getPath());
            logger.debug("Exception details:", ex);
            //throw new IllegalStateException(ex);
        }

    }

    protected void close(final HmConnection connection) {
        HmLogicInterface config = connection.baseResource;
        try {
            if (connection.t != null) {
                connection.t.destroy();
            }
            if (appman != null && connections.isEmpty()) { 
                appman.getResourceAccess().removeResourceDemand(HmDevice.class, devResourceListener);
            }
            config.installationMode().stateControl().removeValueListener(connection.installModeListener);
            config.installationMode().stateFeedback().removeValueListener(connection.installModeListener);
            if (connection.client != null) {
            	// service unregistration in stop method may block(?)
            	new Thread(new Runnable() {
					
					@Override
					public void run() {
						 if (connection.hm != null)
							 connection.hm.close();
						 if (connection.commandLine != null)
							 connection.commandLine.unregister();
					}
				}).start();
            }
            final Thread initThread = connection.initThread;
            if (initThread != null && initThread.isAlive())
            	initThread.interrupt();
            if (logger  != null)
            	logger.info("HomeMatic configuration removed: {}", config);
        } catch (Exception e) {
        	if (logger != null)
        		logger.error("HomeMatic XmlRpc driver shutdown failed", e);
        }
    }

    protected HmConnection findConnection(HmDevice dev) {
        Resource p = dev;
        while (p.getParent() != null) {
            p = p.getParent();
        }
        if (!(p instanceof HmLogicInterface)) {
            throw new IllegalStateException("HmDevice in wrong place: " + dev.getPath());
        }
        return connections.get((HmLogicInterface) p);
    }

    protected void setupDevice(HmDevice dev) {
        String address = dev.address().getValue();
        if (acceptedDevices.containsKey(address)) {
            logger.debug("device {} already controlled by handler type {}", address, acceptedDevices.get(address));
            return;
        }
        HmConnection conn = findConnection(dev);
        if (conn == null) {
            logger.warn("no connection for device {}", dev.getPath());
            return;
        }
        try {
            DeviceDescription channelDesc = conn.client.getDeviceDescription(address);
            if (channelDesc.isDevice()) {
                dev.addStructureListener(new DeletionListener(address, conn.client, true, logger));
            }
            for (DeviceHandler h : conn.handlers) {
                if (h.accept(channelDesc)) {
                    logger.debug("handler available for {}: {}", address, h.getClass().getCanonicalName());
                    Map<String, Map<String, ParameterDescription<?>>> paramSets = new HashMap<>();
                    for (String set : dev.paramsets().getValues()) {
                        logger.trace("requesting paramset {} of device {}", set, address);
                        paramSets.put(set, conn.client.getParamsetDescription(address, set));
                    }
                    HmDevice masterDevice = channelDesc.isDevice() ?
                            dev : (HmDevice) dev.getParent().getParent();
                    h.setup(masterDevice, channelDesc, paramSets);
                    acceptedDevices.put(address, h.getClass().asSubclass(DeviceHandler.class));
                    break;
                }
            }
        } catch (XmlRpcException ex) {
            logger.error("failed to configure value resources for device " + dev.getPath(), ex);
        }
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

    // TODO: cover case that clientUrl is not an IP address but a domain name; probably not too relevant, though
    private static NetworkInterface getBestMatchingInterface(final String clientUrl) throws SocketException {
        if (isOwnLoopbackAddress(clientUrl)) {
            return NetworkInterface.getByName("lo");
        }
        String targetAddress = clientUrl;
        try {
            targetAddress = new URL(clientUrl).getHost();
        } catch (MalformedURLException ignore) {
        }
        final Enumeration<?> e = NetworkInterface.getNetworkInterfaces();
        // the higher the key, the better the interfaces match the clientUrl
        final NavigableMap<Integer, List<NetworkInterface>> matches = new TreeMap<>();
        while (e.hasMoreElements()) {
            NetworkInterface n = (NetworkInterface) e.nextElement();
            final Enumeration<?> ee = n.getInetAddresses();
            int cnt = -1;
            while (ee.hasMoreElements()) {
                InetAddress i = (InetAddress) ee.nextElement();
                if (!(i instanceof Inet4Address) && !(i instanceof Inet6Address)) {
                    continue;
                }
                final int level = getAgreementLevel(i.getHostAddress(), targetAddress);
                if (level > cnt) {
                    cnt = level;
                }
            }
            if (cnt >= 0) {
                List<NetworkInterface> ifs = matches.get(cnt);
                if (ifs == null) {
                    ifs = new ArrayList<>();
                    matches.put(cnt, ifs);
                }
                ifs.add(n);
            }
        }
        final Logger logger = LoggerFactory.getLogger(HomeMaticDriver.class);
        if (matches.isEmpty()) {
            logger.error("No network interfaces found, cannot start driver");
            return null;
        }
        final Map.Entry<Integer, List<NetworkInterface>> entry = matches.lastEntry();
        final List<NetworkInterface> ifs = entry.getValue();
        final NetworkInterface selected = ifs.get(0);
        if (ifs.size() > 1) {
            logger.warn("Local matching interface not unique for clientUrl {}, "
                    + "found {} candidates, at agreement level {}. Selecting the first: {}", clientUrl, ifs.size(), entry.getKey(), selected);
        } else {
            logger.info("Local matching interface for clientUrl {} is {}, at agreement level {}", clientUrl, selected, entry.getKey());
        }
        return selected;
    }

    private static int getAgreementLevel(String address, String targetAddress) {
        final int sz = Math.min(address.length(), targetAddress.length());
        for (int i = 0; i < sz; i++) {
            if (address.charAt(i) != targetAddress.charAt(i)) {
                return i;
            }
        }
        return sz;
    }

    private static boolean isOwnLoopbackAddress(final String clientUrl) {
        return clientUrl.contains("localhost") || clientUrl.contains("127.0.0.1") || clientUrl.contains("0:0:0:0:0:0:0:1")
                || clientUrl.contains("::1");
    }

}
