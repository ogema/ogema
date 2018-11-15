/**
 * Copyright 2011-2018 Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;

import org.apache.xmlrpc.XmlRpcException;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.application.Timer;
import org.ogema.core.application.TimerListener;
import org.ogema.core.model.Resource;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.resourcemanager.ResourceDemandListener;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.drivers.homematic.xmlrpc.hl.api.DeviceHandler;
import org.ogema.drivers.homematic.xmlrpc.hl.api.DeviceHandlerFactory;
import org.ogema.drivers.homematic.xmlrpc.hl.api.HomeMaticConnection;
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
import org.ogema.drivers.homematic.xmlrpc.ll.api.HmEventListener;
import org.ogema.drivers.homematic.xmlrpc.ll.api.HomeMatic;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HmConnection implements HomeMaticConnection {
	private final ApplicationManager appman;
    //private final EventAdmin eventAdmin;
    private final ComponentContext ctx;
    private final Logger logger;
    private final HomeMaticDriver hmDriver;
    
    final int MAX_RETRIES = 5;

    private final Runnable initTask;
    protected long reInitTryTime = 2*60000;
    public static final long MAX_REINITTRYTIME = 24*60*60000;
    // synchronized on this
	private Thread initThread;
	HomeMaticService hm;
    HomeMatic client;
    final HmLogicInterface baseResource;
    ServiceRegistration<HomeMaticClientCli> commandLine;
    // FIXME better use Java time than OGEMA timer in hardware driver?
    volatile Timer t;
    final WriteScheduler writer;
    /* Chain of responsibility for handling HomeMatic devices, the first
     handler that accepts a DeviceDescription will control that device 
     (=> register more specific handlers first; not actually useful for standard
     homematic device types) */
    final List<DeviceHandler> handlers;
    
    public static Resource getToplevelResource(Resource r) {
        Resource res = r.getLocationResource();
        while(!res.isTopLevel()) {
            res = res.getParent();
            if(res == null) throw new IllegalStateException("This should never occur!");
        }
        return res;
    }

    private final ResourceDemandListener<HmDevice> devResourceListener = new ResourceDemandListener<HmDevice>() {

        @Override
        public void resourceAvailable(HmDevice t) {
        	if(!getToplevelResource(t).equalsLocation(baseResource)) return;
            hmDriver.setupDevice(t);
        }

        @Override
        public void resourceUnavailable(HmDevice t) {
            //TODO
        }

    };

    public HmConnection(List<DeviceHandlerFactory> handlerFactories, 
    		final ApplicationManager appman, EventAdmin eventAdmin, ComponentContext ctx, final Logger logger,
    		HomeMaticDriver hmDriver, final HmLogicInterface baseResource) {
		this.appman = appman;
		this.baseResource = baseResource;
		//this.eventAdmin = eventAdmin;
		this.ctx = ctx;
		this.logger = logger;
		this.hmDriver = hmDriver;
        writer = new WriteScheduler(appman, eventAdmin);
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
        
        this.initTask = new Runnable() {
			
        	@Override
            public void run() {
                logger.debug("Starting Homematic init for ogema-"+baseResource.getName()+", may block for 20sec");
                try { // blocks for ca. 20s if no connection can be established
                    hm.init(client, "ogema-" + baseResource.getName());
                } catch (XmlRpcException e) {
                    if (Thread.interrupted()) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                    logger.error("could not start HomeMatic driver for config {}", baseResource.getPath());
                    logger.debug("Exception details:", e);
                    performInitThreadDelayed();
                    return;
                }
                if (Thread.interrupted()) {
                    Thread.currentThread().interrupt();
                    logger.error("Thread interrupted for config {}", baseResource.getPath());
                    return;
                }
                appman.getResourceAccess().addResourceDemand(HmDevice.class, devResourceListener);
                t = appman.createTimer(30000, installModePolling);
                writer.start();
                logger.info("HomeMatic driver configured and registered according to config {}", baseResource.getPath());
            }
		};
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
    public <T> T getValue(String address, String value_key) throws IOException {
        try {
            return client.getValue(address, value_key);
        } catch (XmlRpcException e) {
            throw new IOException(e.getMessage());
        }
    }
    
    @Override
    public void performSetValue(String address, String valueKey, Object value) {
        writer.addWriteAction(WriteAction.createSetValue(client, address, valueKey, value));
    }

    @Override
    public void performPutParamset(String address, String set, Map<String, Object> values) {
        writer.addWriteAction(WriteAction.createPutParamset(client, address, set, values));
    }

    @Override
    public void performAddLink(String sender, String receiver, String name, String description) {
        WriteAction writeAction = WriteAction.createAddLink(client, sender, receiver, name, description);
        writer.addWriteAction(writeAction);
    }

    @Override
    public void performRemoveLink(String sender, String receiver) {
        writer.addWriteAction(WriteAction.createRemoveLink(client, sender, receiver));
    }

    @Override
    public List<Map<String, Object>> performGetLinks(String address, int flags) {
        try {
            logger.debug("get links for {}", address);
            return client.getLinks(address, flags);
        } catch (XmlRpcException ex) {
            logger.error("getLinks failed for {}", address, ex);
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
    
    
    public void init() {
        try {
            String serverUrl = null;
            final HmLogicInterface config = baseResource;
            String urlPattern = config.baseUrl().getValue();
            // FIXME double use of the baseUrl resource is somewhat risky... is it really intended that one can either specify
            // an URL (if networkInterface is not set) or a pattern for the address (if networkInterface is also set)?
            if (urlPattern == null || urlPattern.isEmpty()) {
                urlPattern = "http://%s:%d";
            }
            String alias = config.alias().getValue();
            String iface = config.networkInterface().getValue();
            final boolean ifSet = config.networkInterface().isActive() || config.baseUrl().isActive();
            List<NetworkInterface> n = null;
            if (!ifSet) {
                n = getBestMatchingInterfaces(config.clientUrl().getValue());
                if (n == null) {
                    throw new IllegalArgumentException(
                            "Bad configuration: Network interface or base url not set and failed to determine interface automatically");
                } else {
                    logger.debug("network interface selected for {}: {}", config.clientUrl().getValue(), n);
                }
            }
            if (!ifSet || (iface != null && !iface.isEmpty())) {
            	final List<NetworkInterface> nifs;
            	if (n != null)
            		nifs = n;
            	else {
            		final NetworkInterface nif = NetworkInterface.getByName(iface);
                	if (nif == null) {
                        throw new IllegalStateException("no such network interface: " + iface);
                    }
                	nifs = Collections.singletonList(nif);
            	}
            	Inet4Address i4address = null;
            	for (NetworkInterface nif : nifs) {
            		i4address = getAddressFromInterface(nif);
            		if (i4address != null)
            			break;
            	}
                if (i4address == null) {
                    throw new IllegalArgumentException("could not determine IP address for interface " + iface);
                }
                logger.info("Selected IPv4 address for own network interface {}", i4address);
                final String ipAddrString = i4address.getHostAddress();
                serverUrl = String.format(urlPattern, ipAddrString, config.port().getValue());
            } else {
                serverUrl = urlPattern + alias;
            }

            client = new HomeMaticClient(config.clientUrl().getValue());
            commandLine = new HomeMaticClientCli(client).register(ctx.getBundleContext(), config.getName());

            hm = new HomeMaticService(ctx.getBundleContext(), serverUrl, alias);

            config.installationMode().stateControl().create();
            config.installationMode().stateFeedback().create();
            config.installationMode().activate(true);
            config.installationMode().stateControl().addValueListener(installModeListener, true);
            config.installationMode().stateFeedback().addValueListener(installModeListener, false);
            Persistence persistence = new Persistence(appman, config);
            hm.setBackend(persistence);

            hm.addDeviceListener(persistence);
            performInitThread();
        } catch (IOException ex) {
            logger.error("could not start HomeMatic driver for config {} (2)", baseResource.getPath());
            logger.debug("Exception details:", ex);
            //throw new IllegalStateException(ex);
        }
    }
    
    protected void performInitThreadDelayed() {
    	logger.info("Will retry init for config {} after "+(reInitTryTime/1000)+" seconds.", baseResource.getPath());
    	appman.createTimer(reInitTryTime, new TimerListener() {
			
			@Override
			public void timerElapsed(Timer timer) {
				timer.destroy();
				performInitThread();
			}
		});
		reInitTryTime *= 2;
		if(reInitTryTime > MAX_REINITTRYTIME) {
			reInitTryTime = MAX_REINITTRYTIME;
		}
    }
    
    // no synchronization because it is only executed in the app thread
    protected void performInitThread() {
    	if (initThread == null || !initThread.isAlive()) {
	        initThread = new Thread(initTask, "homematic-xmlrpc-init");
	        initThread.start();
    	}
    }
    
    protected void close() {
        HmLogicInterface config = baseResource;
        try {
            if (t != null) {
                t.destroy();
            }
            if (appman != null)
                appman.getResourceAccess().removeResourceDemand(HmDevice.class, devResourceListener);
            config.installationMode().stateControl().removeValueListener(installModeListener);
            config.installationMode().stateFeedback().removeValueListener(installModeListener);
            if (client != null) {
                // service unregistration in stop method may block(?)
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        if (hm != null) {
                            hm.close();
                        }
                        if (commandLine != null) {
                            commandLine.unregister();
                        }
                    }
                }).start();
            }
            if (initThread != null && initThread.isAlive()) {
                initThread.interrupt();
            }
            writer.close();
            if (logger != null) {
                logger.info("HomeMatic configuration removed: {}", config);
            }
        } catch (Exception e) {
            if (logger != null) {
                logger.error("HomeMatic XmlRpc driver shutdown failed", e);
            }
        }
    }
    
    private static Inet4Address getAddressFromInterface(final NetworkInterface nif) {
    	final Enumeration<InetAddress> addresses = nif.getInetAddresses();
        while (addresses.hasMoreElements()) {
            InetAddress a = addresses.nextElement();
            if (a instanceof Inet4Address)
                return (Inet4Address) a;
        }
        return null;
    }

    // TODO: cover case that clientUrl is not an IP address but a domain name; probably not too relevant, though
    private static List<NetworkInterface> getBestMatchingInterfaces(final String clientUrl) throws SocketException {
        if (isOwnLoopbackAddress(clientUrl)) {
            return Collections.singletonList(NetworkInterface.getByName("lo"));
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
        final Iterator<List<NetworkInterface>> it = matches.descendingMap().values().iterator();
        final List<NetworkInterface> list = new ArrayList<>();
        while (it.hasNext()) {
        	list.addAll(it.next());
        }
        return list;
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
