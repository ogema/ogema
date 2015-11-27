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
package org.ogema.driver.zwave;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.ogema.core.channelmanager.ChannelAccess;
import org.ogema.core.channelmanager.driverspi.ChannelDriver;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.ChannelScanListener;
import org.ogema.core.channelmanager.driverspi.ChannelUpdateListener;
import org.ogema.core.channelmanager.driverspi.DeviceListener;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.channelmanager.driverspi.DeviceScanListener;
import org.ogema.core.channelmanager.driverspi.NoSuchChannelException;
import org.ogema.core.channelmanager.driverspi.NoSuchDeviceException;
import org.ogema.core.channelmanager.driverspi.NoSuchInterfaceException;
import org.ogema.core.channelmanager.driverspi.SampledValueContainer;
import org.ogema.core.channelmanager.driverspi.ValueContainer;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.hardwaremanager.HardwareDescriptor;
import org.ogema.core.hardwaremanager.HardwareListener;
import org.ogema.core.hardwaremanager.HardwareManager;
import org.ogema.core.hardwaremanager.UsbHardwareDescriptor;
import org.ogema.driver.zwave.manager.LocalDevice;
import org.ogema.driver.zwave.manager.Node;
import org.slf4j.Logger;

/**
 * 
 * @author baerthbn
 * 
 */
public class ZWaveDriver implements ChannelDriver, HardwareListener {
	private final Logger logger = org.slf4j.LoggerFactory.getLogger("zwave-driver");
	private final Map<String, Connection> connectionsMap; // <interfaceId, connection>
	private final Map<ChannelUpdateListener, List<Channel>> listenerMap;
	private final String driverId = "zwave-driver";
	private final String description = "OGEMA ZWave Driver";
	public final ChannelAccess channelAccess;

	ArrayList<DeviceListener> devListeners;
	private LocalDevice mainLocal;

	private final Object connectionLock = new Object();
	private HardwareManager hwMngr;

	public ZWaveDriver(ChannelAccess channelAccess, HardwareManager hwMngr) {
		this.connectionsMap = new HashMap<>();
		this.listenerMap = new HashMap<>();
		this.devListeners = new ArrayList<>();
		this.channelAccess = channelAccess;
		this.hwMngr = hwMngr;
		hwMngr.addListener(this);
	}

	protected void addConnection(Connection con) {
		mainLocal = con.getLocalDevice();
		connectionsMap.put(con.getInterfaceId(), con);
		con.getLocalDevice().addDeviceListeners(devListeners);
	}

	protected void removeConnection(String interfaceId) {
		Connection con = connectionsMap.remove(interfaceId);
		con.getLocalDevice().removeDeviceListeners(devListeners);
	}

	protected Map<String, Connection> getConnections() {
		return connectionsMap;
	}

	protected Connection findConnection(String interfaceId) {
		return connectionsMap.get(interfaceId);
	}

	@Override
	public String getDriverId() {
		return driverId;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void startDeviceScan(String interfaceId, String filter, DeviceScanListener listener)
			throws UnsupportedOperationException, NoSuchInterfaceException, IOException {
		boolean success = false;
		if (interfaceId != null) {
			Connection connection = connectionsMap.get(interfaceId);
			if (connection.getLocalDevice().isReady()) {
				for (Entry<Short, Node> deviceEntry : connection.getLocalDevice().getNodes().entrySet()) {
					Node node = deviceEntry.getValue();
					if (node.isReady() && node.getNodeId() != connection.getLocalDevice().getNodeId()) {
						String parameters = node.getProductString() + ":" + node.getProductName();
						DeviceLocator deviceLocator = channelAccess.getDeviceLocator(driverId, interfaceId, node
								.getNodeName(), parameters);
						logger.debug("\nDevice found:\nAddress = " + deviceLocator.getDeviceAddress()
								+ "\nDeviceParameters = " + deviceLocator.getParameters());
						listener.deviceFound(deviceLocator);
						success = true;
					}
				}
			}
		}
		else {
			Set<Entry<String, Connection>> connections = connectionsMap.entrySet();
			if (connections.size() <= 0)
				logger.debug("Currently there are no connections alive to ZWave coordinator hardware.");
			else
				for (Entry<String, Connection> con : connections) {
					if (con.getValue().getLocalDevice().isReady()) {
						for (Entry<Short, Node> deviceEntry : con.getValue().getLocalDevice().getNodes().entrySet()) {
							Node node = deviceEntry.getValue();
							if (node.isReady() && node.getNodeId() != con.getValue().getLocalDevice().getNodeId()) {
								String parameters = node.getProductString() + ":" + node.getProductName();
								DeviceLocator deviceLocator = channelAccess.getDeviceLocator(driverId, con.getValue()
										.getInterfaceId(), node.getNodeName(), parameters);
								logger.debug("\nDevice found:\nAddress = " + deviceLocator.getDeviceAddress()
										+ "\nDeviceParameters = " + deviceLocator.getParameters());
								listener.deviceFound(deviceLocator);
								success = true;
							}
						}
					}
				}
		}
		listener.finished(success, null);
	}

	@Override
	public void abortDeviceScan(String interfaceId, String filter) {
		throw new UnsupportedOperationException(); // TODO how?
	}

	@Override
	public void startChannelScan(DeviceLocator device, ChannelScanListener listener) {
		String params = device.getParameters();
		String id = params.substring(params.lastIndexOf(':') + 1, params.length());
		mainLocal.printNodes();
		Node node = mainLocal.nodes.get(Short.valueOf(id));
		logger.debug(String.format("ChannelScanListener registered for node %d", Short.valueOf(id)));
		node.addChannelListener(listener);
	}

	@Override
	public List<ChannelLocator> getChannelList(DeviceLocator device) throws UnsupportedOperationException {
		return connectionsMap.get(device.getInterfaceName()).findDevice(device).getChannelLocators();
	}

	@Override
	public void readChannels(List<SampledValueContainer> channels) throws UnsupportedOperationException, IOException {
		for (SampledValueContainer container : channels) {
			ChannelLocator channelLocator = container.getChannelLocator();

			try {
				Connection con = findConnection(channelLocator.getDeviceLocator().getInterfaceName());
				Device dev = con.findDevice(channelLocator.getDeviceLocator());
				Channel channel = dev.findChannel(channelLocator);

				// read data
				container.setSampledValue(channel.readValue(con));

			} catch (NullPointerException e) {
				throw new IOException("Unknown channel: " + channelLocator, e);
			}
		}
	}

	@Override
	public void listenChannels(List<SampledValueContainer> channels, ChannelUpdateListener listener)
			throws UnsupportedOperationException, NoSuchDeviceException, NoSuchChannelException, IOException {

		logger.debug("listenChannels");
		List<Channel> channelList = listenerMap.get(listener);

		if (channelList == null) {
			channelList = new ArrayList<Channel>();
		}

		for (SampledValueContainer container : channels) {
			ChannelLocator channelLocator = container.getChannelLocator();

			try {
				DeviceLocator dev = channelLocator.getDeviceLocator();
				Channel channel = findConnection(dev.getInterfaceName()).findDevice(dev).findChannel(channelLocator);
				channel.setEventListener(container, listener);
				channelList.add(channel);

			} catch (NullPointerException e) {
				throw new IOException("Unknown channel: " + channelLocator, e);
			}
		}
		listenerMap.put(listener, channelList);
	}

	@Override
	public void writeChannels(List<ValueContainer> channels) throws UnsupportedOperationException, IOException,
			NoSuchDeviceException, NoSuchChannelException {
		for (ValueContainer container : channels) {
			ChannelLocator channelLocator = container.getChannelLocator();

			try {
				Connection con = findConnection(channelLocator.getDeviceLocator().getInterfaceName());
				Device dev = con.findDevice(channelLocator.getDeviceLocator());
				Channel channel = dev.findChannel(channelLocator);
				channel.writeValue(con, container.getValue());

			} catch (NullPointerException e) {
				throw new IOException("Unknown channel: " + channelLocator, e);
			}
		}
	}

	@Override
	public void channelAdded(ChannelLocator channel) {
		DeviceLocator device = channel.getDeviceLocator();
		String iface = device.getInterfaceName();

		Connection con;
		Device dev;
		Channel chan;

		// Connection handling
		con = findConnection(iface);

		if (con == null) {
			return;
		}

		// Device handling
		dev = con.findDevice(device);

		if (dev == null) {
			dev = new Device(device, con);
			con.addDevice(dev);
		}

		// Channel handling
		chan = dev.findChannel(channel);

		if (chan == null) {
			chan = new Channel(channel, dev);
			dev.addChannel(chan);
		}
	}

	@Override
	public void channelRemoved(ChannelLocator channel) {
		DeviceLocator device = channel.getDeviceLocator();
		Connection con = findConnection(device.getInterfaceName());
		Device dev = con.findDevice(device);
		Channel chan = null;
		chan = dev.findChannel(channel);
		if (chan != null)
			dev.removeChannel(chan);
	}

	@Override
	public void hardwareAdded(HardwareDescriptor descriptor) {
		/* This function is responsible for what to do when new hardware (e.g. usb stick) is added */
		logger.debug("hardware added: " + descriptor.getIdentifier().toString());
		establishConnection();
	}

	@Override
	public void hardwareRemoved(HardwareDescriptor descriptor) {
		logger.debug("hardware removed: " + descriptor.getIdentifier().toString());
		String portName = ((UsbHardwareDescriptor) descriptor).getPortName();
		if (portName != null) {
			Connection con = connectionsMap.get(portName);
			if (con != null)
				con.close();
		}
	}

	@Override
	public void shutdown() {

	}

	@Override
	public void addDeviceListener(DeviceListener listener) {
		if (listener == null)
			throw new NullPointerException("Listener object mustn't be null!");
		devListeners.add(listener);
		Set<Entry<String, Connection>> connections = connectionsMap.entrySet();
		for (Entry<String, Connection> con : connections) {
			LocalDevice dev = con.getValue().getLocalDevice();
			dev.addDeviceListener(listener);
		}
	}

	@Override
	public void removeDeviceListener(DeviceListener listener) {
		if (listener == null)
			throw new NullPointerException("Listener object mustn't be null!");
		devListeners.remove(listener);
		Set<Entry<String, Connection>> connections = connectionsMap.entrySet();
		for (Entry<String, Connection> con : connections) {
			LocalDevice dev = con.getValue().getLocalDevice();
			dev.removeDeviceListener(listener);
		}
	}

	public DeviceLocator createDeviceLocator(String ifaceName, Node node) {
		String prodString = node.getProductString();
		String prodName = node.getProductName();
		String nodeName = node.readNodeName();
		// It can take a while until product string and product name are valid for a newly added node
		int tries = 5;
		while (tries-- > 0) {
			prodName = node.getProductName();
			prodString = node.getProductString();
			if (!prodName.equals("") && !prodString.equals(".."))
				break;
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
			}
		}
		logger.debug(String.format("CreateDeviceLocator: ProductString = %s, ProductName = %s, NodeName = %s",
				prodString, prodName, nodeName));
		if (prodName.equals("") || prodName == null)
			return null;
		String parameters = prodString + ":" + prodName + ":" + node.getNodeId();
		DeviceLocator deviceLocator = channelAccess.getDeviceLocator(driverId, ifaceName, nodeName, parameters);
		return deviceLocator;
	}

	public void establishConnection() {
		final String portName = getPortName(hwMngr);
		Connection con = connectionsMap.get(portName);
		if (con == null && portName != null) {
			final ZWaveDriver driver = this;
			Thread connectThread = new Thread() {
				@Override
				public void run() {

					Connection con = new Connection(portName, connectionLock, hwMngr, driver);
					synchronized (connectionLock) {
						while (!con.hasConnection() && Activator.bundleIsRunning) {
							try {
								connectionLock.wait();
							} catch (InterruptedException ex) {
								ex.printStackTrace();
							}
						}
					}
					addConnection(con);
				}
			};
			connectThread.setName("zwave-connect");
			connectThread.start();
		}
		else if (con != null) {
			con.getLocalDevice().restart();
		}
		else {
			logger.info("No portname could be determined!");
		}
	}

	String getPortName(HardwareManager hwMngr) {
		String portName = System.getProperty(Constants.STATIC_IF_NAME);
		if (portName == null) {
			String hardwareDesriptors = System.getProperty(Constants.HARDWARE_DESCRIPTOR,
					Constants.DEFAULT_HW_DESCRIPTOR);
			logger.info(String.format(
					"No device file specified on the command line. The Hardware descriptor %s is used instead.",
					hardwareDesriptors));
			// Collection<HardwareDescriptor> descriptors = hwMngr.getHardwareDescriptors(".+:0658:0200:");
			Collection<HardwareDescriptor> descriptors = hwMngr.getHardwareDescriptors(hardwareDesriptors);
			// logger.info(
			// String.format("Portname via hardware descriptor: %s.%s", hardwareDesriptors, descriptors.size()));
			for (HardwareDescriptor descr : descriptors) {
				portName = ((UsbHardwareDescriptor) descr).getPortName();
				if (portName != null)
					break;
			}
		}
		logger.info(String.format("Port name detected %s", portName));
		return portName;
	}

	@Override
	public void writeChannel(ChannelLocator channelLocator, Value value) throws UnsupportedOperationException,
			IOException, NoSuchDeviceException, NoSuchChannelException {
		try {
			Connection con = findConnection(channelLocator.getDeviceLocator().getInterfaceName());
			Device dev = con.findDevice(channelLocator.getDeviceLocator());
			Channel channel = dev.findChannel(channelLocator);
			channel.writeValue(con, value);

		} catch (NullPointerException e) {
			throw new IOException("Unknown channel: " + channelLocator, e);
		}
	}

}
