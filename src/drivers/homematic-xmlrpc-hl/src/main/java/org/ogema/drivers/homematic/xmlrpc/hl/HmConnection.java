/**
 * Copyright 2011-2019 Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
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
import java.net.InterfaceAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.apache.xmlrpc.XmlRpcException;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.StringResource;
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
import org.ogema.drivers.homematic.xmlrpc.hl.discovery.Ccu;
import org.ogema.drivers.homematic.xmlrpc.hl.discovery.UdpDiscovery;
import org.ogema.drivers.homematic.xmlrpc.hl.types.HmDevice;
import org.ogema.drivers.homematic.xmlrpc.hl.types.HmLogicInterface;
import org.ogema.drivers.homematic.xmlrpc.ll.HomeMaticClient;
import org.ogema.drivers.homematic.xmlrpc.ll.HomeMaticClientCli;
import org.ogema.drivers.homematic.xmlrpc.ll.HomeMaticService;
import org.ogema.drivers.homematic.xmlrpc.ll.api.HmEvent;
import org.ogema.drivers.homematic.xmlrpc.ll.api.HmEventListener;
import org.ogema.drivers.homematic.xmlrpc.ll.api.HomeMatic;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HmConnection implements HomeMaticConnection {

	private final ApplicationManager appman;
	// private final EventAdmin eventAdmin;
	private final ComponentContext ctx;
	public static final Logger logger = LoggerFactory.getLogger(HomeMaticDriver.class);
	private final HomeMaticDriver hmDriver;

	final int MAX_RETRIES = 5;

	// private final Runnable initTask;
	protected long reInitTryTime = 2 * 60000;
	public static final long MAX_REINITTRYTIME = 24 * 60 * 60000;
	private Thread connectionThread;

	// quasi-final: not changed after init() call
	HomeMaticService hm;
	// quasi-final: not changed after init() call
	HomeMatic client;
	final HmLogicInterface baseResource;
	// quasi-final: not changed after init() call
	ServiceRegistration<HomeMaticClientCli> commandLine;

	private final ScheduledExecutorService t = Executors.newSingleThreadScheduledExecutor();
	ScheduledFuture<?> installModePoller;
	ScheduledFuture<?> pingCheck;

	final WriteScheduler writer;
	/*
	 * Chain of responsibility for handling HomeMatic devices, the first handler
	 * that accepts a DeviceDescription will control that device (=> register more
	 * specific handlers first; not actually useful for standard homematic device
	 * types)
	 */
	final List<DeviceHandler> handlers;

	public static Resource getToplevelResource(Resource r) {
		Resource res = r.getLocationResource();
		while (!res.isTopLevel()) {
			res = res.getParent();
			if (res == null) {
				throw new IllegalStateException("This should never occur!");
			}
		}
		return res;
	}

	private final ResourceDemandListener<HmDevice> devResourceListener = new ResourceDemandListener<HmDevice>() {

		@Override
		public void resourceAvailable(HmDevice t) {
			if (!getToplevelResource(t).equalsLocation(baseResource)) {
				return;
			}
			hmDriver.setupDevice(t);
		}

		@Override
		public void resourceUnavailable(HmDevice t) {
			// TODO
		}

	};

	public HmConnection(List<DeviceHandlerFactory> handlerFactories, final ApplicationManager appman,
			EventAdmin eventAdmin, ComponentContext ctx, HomeMaticDriver hmDriver,
			final HmLogicInterface baseResource) {
		this.appman = appman;
		this.baseResource = baseResource;
		// this.eventAdmin = eventAdmin;
		this.ctx = ctx;
		this.hmDriver = hmDriver;
		writer = new WriteScheduler(appman, eventAdmin);
		this.handlers = new ArrayList<>();
		for (DeviceHandlerFactory fac : handlerFactories) {
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
		writer.start();
	}

	private void connect() {
		logger.debug("Starting Homematic init for ogema-" + baseResource.getName() + ", may block for 20sec");
		try { // blocks for ca. 20s if no connection can be established
			hm.init(client, "ogema-" + baseResource.getName());
		} catch (XmlRpcException e) {
			if (Thread.interrupted()) {
				Thread.currentThread().interrupt();
				return;
			}
			logger.error("could not start HomeMatic driver for config {}", baseResource.getPath());
			logger.debug("Exception details:", e);
			retryConnect();
			return;
		}
		if (Thread.interrupted()) {
			Thread.currentThread().interrupt();
			logger.error("Thread interrupted for config {}", baseResource.getPath());
			return;
		}
		appman.getResourceAccess().addResourceDemand(HmDevice.class, devResourceListener);
		if (installModePoller == null) {
			installModePoller = t.scheduleWithFixedDelay(installModePolling, 0, 60, TimeUnit.SECONDS);
		}
		if (pingCheck == null) {
			pingCheck = t.scheduleWithFixedDelay(new Runnable() {
				@Override
				public void run() {
					checkPing();
				}
			}, 10, 60, TimeUnit.SECONDS);
		}
		logger.info("HomeMatic driver configured and registered according to config {}", baseResource.getPath());
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

	final Runnable installModePolling = new Runnable() {

		@Override
		public void run() {
			try {
				int secondsRemaining = client.getInstallMode();
				logger.debug("polled installation mode: {}s", secondsRemaining);
				baseResource.installationMode().stateFeedback().setValue(secondsRemaining > 0);
			} catch (XmlRpcException ex) {
				logger.error("could not poll HomeMatic client for installation mode state", ex);
			}
		}
	};

	Future<Boolean> checkPing() {
		final String callerId = "ogema" + System.currentTimeMillis();
		final CountDownLatch pongReceived = new CountDownLatch(1);
		final long pingTimeoutSec = 10;

		final HmEventListener pong = new HmEventListener() {
			@Override
			public void event(List<HmEvent> events) {
				if (events.size() == 1) {
					HmEvent e = events.get(0);
					if ("PONG".equals(e.getValueKey())) {
						// BidCos vs IP
						if ("CENTRAL".equals(e.getAddress()) || "CENTRAL:0".equals(e.getAddress())) {
							if (callerId.equals(e.getValueString())) {
								pongReceived.countDown();
							}
						}
					}
				}
			}
		};
		addEventListener(pong);

		return t.submit(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				try {
					client.ping(callerId);
					if (pongReceived.await(pingTimeoutSec, TimeUnit.SECONDS)) {
						logger.debug("ping OK");
						return true;
					} else {
						logger.warn("Homematic service does not respond, registering again...");
						performConnect();
						return false;
					}
				} catch (XmlRpcException ex) {
					// TODO
					logger.warn("PING failed", ex);
					return false;
				} finally {
					removeEventListener(pong);
				}
			}
		});
	}

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
	 * Returns the HmDevice element controlling the given OGEMA resource, or null if
	 * the resource is not controlled by the HomeMatic driver.
	 *
	 * @param ogemaDevice
	 * @return HomeMatic device resource controlling the given resource or null.
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public HmDevice findControllingDevice(Resource ogemaDevice) {
		// XXX: review this mess
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

	/*
	 * TODO: error handling. If this fails (e.g. CCU not available) then we should retry later.
	 */
	public void init() {
		try {
			final HmLogicInterface config = baseResource;
			final String serverUrl; // driver servlet url
			String xmlRpcServiceUrl = null; // homematic gateway XML-RPC interface URL
			final List<NetworkInterface> interfaces = new ArrayList<>();
			if (config.networkInterface().isActive() ||
					config.baseUrl().isActive() ||
					config.clientUrl().isActive()) {
				serverUrl = getServerUrl(config, interfaces, xmlRpcServiceUrl);
				if (config.clientUrl().isActive())
					xmlRpcServiceUrl = config.clientUrl().getValue();
				else
					xmlRpcServiceUrl = getXmlRpcInterface(config, interfaces);
			} else {
				if (!config.clientPort().isActive())
					throw new IllegalStateException("Neither client port nor client URL set for config " + config);
				xmlRpcServiceUrl = getXmlRpcInterface(config, interfaces);
				serverUrl = getServerUrl(config, interfaces, xmlRpcServiceUrl);
			}
			if (serverUrl == null || serverUrl.isEmpty()) {
				throw new IllegalStateException("Invalid server or client address " + serverUrl + ", " + xmlRpcServiceUrl);
			}
			if (xmlRpcServiceUrl == null || xmlRpcServiceUrl.isEmpty()) {
				throw new IllegalStateException("CCU XML-RPC service not found. Serial number: " + config.serialNumber().getValue());
			}
			final String alias = getServletAlias(config);
			logger.info("New Homematic XML_RPC connection to {}, servlet URL {}{}", xmlRpcServiceUrl, serverUrl, alias);
			client = new HomeMaticClient(xmlRpcServiceUrl, config.ccuUser().isActive() ? config.ccuUser().getValue() : null, config.ccuPw().isActive() ? config.ccuPw().getValue() : null);
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
			performConnect();
		} catch (IOException ex) {
			logger.error("could not start HomeMatic driver for config {} (2)", baseResource.getPath());
			logger.debug("Exception details:", ex);
			// throw new IllegalStateException(ex);
		}
	}
	
	private static String getServletAlias(final HmLogicInterface config) {
		if (config.alias().isActive())
			return config.alias().getValue();
		final int port;
		try {
			port = config.clientPort().isActive() ? config.clientPort().getValue() : new URL(config.clientUrl().getValue()).getPort();
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
		switch (port) {
		case 2010:
		case 42010:
			return HmLogicInterface.ALIAS + "ip";
		case 2001:
		case 42001:
			return HmLogicInterface.ALIAS;
		case 2000:
		case 42000:
			return HmLogicInterface.ALIAS + "wired";
		default:
			return HmLogicInterface.ALIAS + port;
		}
	}

	private String getServerUrl(final HmLogicInterface config, 
				final List<NetworkInterface> interfaces, final String clientUrl) throws SocketException, UnknownHostException, MalformedURLException {
		String urlPattern = config.baseUrl().getValue();
		if (urlPattern == null || urlPattern.isEmpty()) {
			urlPattern = "http://%s:%d";
		}
		String iface = config.networkInterface().getValue();
		if (interfaces.isEmpty()) {
			if (config.networkInterface().isActive()) {
				final NetworkInterface nif = NetworkInterface.getByName(iface);
				if (nif != null)
					interfaces.add(nif);
			} else if (config.baseUrl().isActive()) {
				final NetworkInterface nif = NetworkInterface.getByInetAddress(Inet4Address.getByName(new URL(config.baseUrl().getValue()).getHost()));
				if (nif != null)
					interfaces.add(nif);
			} else {
				interfaces.addAll(getBestMatchingInterfaces(clientUrl == null ? null : clientUrl.toLowerCase()));
			}
			if (interfaces.isEmpty()) {
				throw new IllegalArgumentException(
						"Bad configuration: Network interface or base url not set and failed to determine interface automatically");
			} else {
				logger.debug("network interfaces selected: {}", interfaces);
			}
		}
		String address;
		if (config.baseUrl().isActive())
			address = config.baseUrl().getValue();
		else {
			Inet4Address i4address = null;
			for (NetworkInterface nif : interfaces) {
				i4address = getAddressFromInterface(nif);
				if (i4address != null) {
					break;
				}
			}
			if (i4address == null) {
				throw new IllegalArgumentException("could not determine IP address for interface " + iface);
			}
			logger.info("Selected IPv4 address for own network interface {}", i4address);
			final String ipAddrString = i4address.getHostAddress();
			int port;
			if (config.port().isActive()) {
				port = config.port().getValue();
			} else {
				port = 8080; // default jetty port
				try {
					port = Integer.parseInt(ctx.getBundleContext().getProperty("org.osgi.service.http.port"));
				} catch (NumberFormatException ok) {
				} catch (SecurityException e) {
					logger.warn("Failed to determine Jetty port",e);
				}
			}
			address = String.format(urlPattern, ipAddrString, port);
		}
		return address;
	}
	
	private static String getXmlRpcInterface(final HmLogicInterface config, final List<NetworkInterface> interfaces) throws SocketException {
		final Collection<Ccu> ccus = runCcuDiscovery(config, interfaces);
		if (ccus.isEmpty()) {
			logger.warn("No CCU found");
			return null;
		}
		if (ccus.size() > 1) 
			logger.warn("More than one matching CCU found: {}", ccus);
		else
			logger.debug("CCUs found: {}", ccus);
		final Ccu ccu = ccus.iterator().next();
		if (!config.serialNumber().isActive()) {
			config.serialNumber().<StringResource> create().setValue(ccu.getSerialNumber());
			config.serialNumber().activate(false);
		}
		if (interfaces.isEmpty())
			interfaces.add(ccu.getNetworkInterface());
		final int port = config.clientPort().getValue();
		// by default, ports 42001 and 42010 are used for BidCos and IP with https, ports 2001 and 2010 with http
		// see https://homematic-forum.de/forum/viewtopic.php?p=465022#p465022
		final String scheme = port > 40000 ? "https" : "http";
		return scheme + "://" + ccu.getAddress().getHostAddress() + ":" + port;
	}

	/**
	 * @param interfaces may be empty, in which case it is to be filled
	 * @return
	 * @throws SocketException
	 */
	private static Collection<Ccu> runCcuDiscovery(final HmLogicInterface config, final List<NetworkInterface> interfaces) throws SocketException {
		final List<NetworkInterface> ifs;
		if (!interfaces.isEmpty())
			ifs = interfaces;
		else {
			ifs = new ArrayList<>();
			final Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
			while (en.hasMoreElements()) {
				ifs.add(en.nextElement());
			}
		}
		// UDP discovery
		final String serialNr = config.serialNumber().isActive() ? config.serialNumber().getValue() : UdpDiscovery.DEFAULT_SERIAL_NR;
		Collection<Ccu> results = null;
		try (final UdpDiscovery udp = new UdpDiscovery()) {
			outer: for (NetworkInterface nif : ifs) {
				for (InterfaceAddress ia : nif.getInterfaceAddresses()) {
					final InetAddress brdc = ia.getBroadcast();
					if (brdc == null)
						continue;
					udp.submit(brdc, nif, serialNr);
					results = udp.getIntermediateResults(0);
					if (results != null)
						break outer; // closes UDP
				}
			}
			if (results == null)
				results = udp.getIntermediateResults(2 * UdpDiscovery.LISTEN_TIMEOUT);
		}
		// determine client url from broadcast to xxx.xxx.xxx.255:43439
		if (results != null && !results.isEmpty()) {
			final Collection<Ccu> ccus;
			if (!serialNr.equals(UdpDiscovery.DEFAULT_SERIAL_NR)) {
				ccus = new ArrayList<>(results.size());
				for (Ccu ccu : results) {
					if (serialNr.equalsIgnoreCase(ccu.getSerialNumber()))
						ccus.add(ccu);
				}
			} else {
				ccus = results;
			}
			return ccus;
		} else
			return Collections.emptyList();

	}

	protected void retryConnect() {
		logger.info("Will retry init for config {} after " + (reInitTryTime / 1000) + " seconds.",
				baseResource.getPath());
		t.schedule(new Runnable() {
			@Override
			public void run() {
				performConnect();
			}
		}, reInitTryTime, TimeUnit.MILLISECONDS);
		reInitTryTime *= 2;
		if (reInitTryTime > MAX_REINITTRYTIME) {
			reInitTryTime = MAX_REINITTRYTIME;
		}
	}

	// no synchronization for connectionThread because it is only executed in the
	// single-threaded scheduler (after init())
	protected void performConnect() {
		if (connectionThread == null || !connectionThread.isAlive()) {
			connectionThread = new Thread(new Runnable() {
				@Override
				public void run() {
					connect();
				}
			}, "homematic-xmlrpc-init");
			connectionThread.start();
		}
	}

	protected void close() {
		HmLogicInterface config = baseResource;
		try {
			t.shutdownNow();
			if (appman != null) {
				appman.getResourceAccess().removeResourceDemand(HmDevice.class, devResourceListener);
			}
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
			if (connectionThread != null && connectionThread.isAlive()) {
				connectionThread.interrupt();
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
			if (a instanceof Inet4Address) {
				return (Inet4Address) a;
			}
		}
		return null;
	}

	// TODO: cover case that clientUrl is not an IP address but a domain name;
	// probably not too relevant, though
	private static List<NetworkInterface> getBestMatchingInterfaces(final String clientUrl) throws SocketException {
		if (isOwnLoopbackAddress(clientUrl)) {
			return Collections.singletonList(NetworkInterface.getByName("lo"));
		}
		String targetAddress = clientUrl;
		try {
			targetAddress = new URL(clientUrl).getHost();
		} catch (MalformedURLException | NullPointerException ignore) {
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
				final int level = targetAddress == null ? 1 : 
						getAgreementLevel(i.getHostAddress().toLowerCase(), targetAddress);
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
		if (clientUrl == null)
			return false;
		return clientUrl.contains("localhost") || clientUrl.contains("127.0.0.1")
				|| clientUrl.contains("0:0:0:0:0:0:0:1") || clientUrl.contains("::1");
	}

}
