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
package org.ogema.driver.xbee;

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
import org.ogema.core.hardwaremanager.HardwareDescriptor.HardwareType;
import org.ogema.core.hardwaremanager.HardwareListener;
import org.ogema.core.hardwaremanager.HardwareManager;
import org.ogema.core.hardwaremanager.SerialHardwareDescriptor;
import org.ogema.core.hardwaremanager.UsbHardwareDescriptor;
import org.ogema.driver.xbee.manager.Endpoint;
import org.ogema.driver.xbee.manager.RemoteDevice;
import org.ogema.driver.xbee.manager.RemoteDevice.InitStates;
import org.ogema.driver.xbee.manager.SimpleDescriptor;
import org.slf4j.Logger;

public class XBeeDriver implements ChannelDriver, HardwareListener {

	private final Map<String, Connection> connectionsMap; // <interfaceId,
	// connection>
	private final String driverId = "xbee-driver";
	private final String description = "ZigBee driver for Series 2 XBees in API Mode";
	private final Map<ChannelUpdateListener, List<Channel>> listenerMap;
	private ChannelAccess channelAccess;
	private final Logger logger = org.slf4j.LoggerFactory.getLogger("xbee-driver");

	public XBeeDriver(ChannelAccess channelAccess, HardwareManager hwMngr) {
		connectionsMap = new HashMap<String, Connection>();
		listenerMap = new HashMap<>();
		hwMngr.addListener(this);
		this.channelAccess = channelAccess;

		// Check if the portname property is set
		String portName = System.getProperty(Constants.STATIC_IF_NAME);
		if (portName != null) {
			Connection con = new Connection(portName, this);
			if (con.localDevice != null)
				addConnection(con);
		}
		else {
			Collection<HardwareDescriptor> descriptors = hwMngr.getHardwareDescriptors(".+:0403:6001:");
			for (HardwareDescriptor descr : descriptors) {
				portName = ((UsbHardwareDescriptor) descr).getPortName();
				if (portName != null) {
					Connection con = new Connection(portName, this);
					if (con.localDevice != null)
						addConnection(con);
				}
			}
		}

	}

	protected void removeConnection(String interfaceId) {
		connectionsMap.remove(interfaceId);
	}

	protected void addConnection(Connection con) {
		connectionsMap.put(con.getInterfaceId(), con);
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

	/**
	 * Scan for initialized devices.
	 */
	@Override
	public void startDeviceScan(String interfaceId, String filter, DeviceScanListener listener)
			throws UnsupportedOperationException, NoSuchInterfaceException, IOException {
		// TODO filter (XBee, ZigBee, deviceId, modelType...)
		boolean success = false;
		Connection connection = null;
		if (interfaceId != null) {
			connection = connectionsMap.get(interfaceId);
			if (connection == null)
				logger.error("There is no connection established over the specified interface.");
			else
				success = scanConnection(connection, listener);
		}
		else {
			Set<Entry<String, Connection>> connections = connectionsMap.entrySet();
			if (connections.size() <= 0)
				logger.debug("Currently there are no connections alive to ZigBee coordinator hardware.");
			else
				for (Entry<String, Connection> con : connections) {
					success = scanConnection(con.getValue(), listener);
				}
		}
		// TODO timer loop
		listener.finished(success, null);
	}

	private boolean scanConnection(Connection connection, DeviceScanListener listener) {
		if (connection == null || connection.localDevice == null) {
			logger.error("No coordinator hardware seems to be pluged in. Please plug your XSTICK in.");
			return false;
		}
		// connection.localDevice.getDeviceHandler().initNetworkScan();
		boolean success = false;
		// TODO wait for the initiated network scan/device initialization to finish? Timer? How long?
		for (Map.Entry<Long, RemoteDevice> deviceEntry : connection.localDevice.getDevices().entrySet()) {
			RemoteDevice remoteDevice = deviceEntry.getValue();
			if (remoteDevice.getInitState() == InitStates.INITIALIZED) {
				for (Map.Entry<Byte, Endpoint> endpointEntry : remoteDevice.getEndpoints().entrySet()) {
					String deviceType = remoteDevice.getDeviceType();
					String nodeIdentifier = remoteDevice.getNodeIdentifier();
					if (nodeIdentifier == null)
						continue; // not realy initialized
					String deviceId = null;
					String profileId = null;
					SimpleDescriptor sd = endpointEntry.getValue().getSimpleDescriptor();
					if (sd != null) {
						deviceId = Integer.toHexString(sd.getApplicationDeviceId() & 0xffff);
						deviceId = ("0000" + deviceId).substring(deviceId.length());
						profileId = Integer.toHexString(sd.getApplicationProfileId() & 0xffff);
						profileId = ("0000" + profileId).substring(profileId.length()); // Needed for leading 0s
					} // Needed for leading 0s

					String parameters = deviceType + ":" + nodeIdentifier + ":" + deviceId + ":" + profileId;
					String address64Bit = Long.toHexString(remoteDevice.getAddress64Bit());
					address64Bit = ("0000000000000000" + address64Bit).substring(address64Bit.length()); // Needed for
					// leading
					// 0s
					String endpointId = Integer.toHexString(endpointEntry.getKey() & 0xff);
					endpointId = ("00" + endpointId).substring(endpointId.length());
					DeviceLocator deviceLocator = channelAccess.getDeviceLocator(driverId, connection.getInterfaceId(),
							address64Bit + ":" + endpointId, parameters);
					listener.deviceFound(deviceLocator);
					success = true;
				}
			}
		}
		return success;
	}

	@Override
	public void abortDeviceScan(String interfaceId, String filter) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void startChannelScan(DeviceLocator device, ChannelScanListener listener)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
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
		if (Configuration.DEBUG)
			logger.debug("listenChannels");
		List<Channel> channelList = listenerMap.get(listener);

		if (channelList == null) { // remove old channels
			channelList = new ArrayList<Channel>();
		}

		for (SampledValueContainer container : channels) {
			ChannelLocator channelLocator = container.getChannelLocator();

			try {
				Connection con = findConnection(channelLocator.getDeviceLocator().getInterfaceName());
				Device dev = con.findDevice(channelLocator.getDeviceLocator());
				if (dev == null) { // create device if it doesn't exist
					channelAdded(channelLocator);
					dev = con.findDevice(channelLocator.getDeviceLocator());
				}
				Channel channel = dev.findChannel(channelLocator);
				if (channel == null) { // create channel if it doesn't exist
					channelAdded(channelLocator);
					channel = dev.findChannel(channelLocator);
				}

				if (!channelLocator.getChannelAddress().split(":")[1].equals("COMMAND")) {
					channel.setUpdateListener(container, listener);
					channelList.add(channel);
				}

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
	public void channelAdded(ChannelLocator channelLocator) {
		DeviceLocator deviceLocator = channelLocator.getDeviceLocator();
		String iface = deviceLocator.getInterfaceName();

		Connection con;
		Device device;
		Channel channel;

		// Connection handling
		con = findConnection(iface);

		if (con == null) {
			con = new Connection(iface, this);
			addConnection(con);
		}

		// Device handling
		device = con.findDevice(deviceLocator);

		if (device == null) {
			device = new Device(deviceLocator, con);
			con.addDevice(device);
		}

		// Channel handling
		channel = device.findChannel(channelLocator);

		if (channel == null) {
			channel = Channel.createChannel(channelLocator, device);
			device.addChannel(channel);
		}
	}

	@Override
	public void channelRemoved(ChannelLocator channelLocator) {
		DeviceLocator deviceLocator = channelLocator.getDeviceLocator();
		String iface = deviceLocator.getInterfaceName();

		Connection con;
		Device device;

		// Connection handling
		con = findConnection(iface);
		if (null == con) {
			return;
		}

		// Device handling
		device = con.findDevice(deviceLocator);
		if (null == device) {
			return;
		}

		device.removeChannel(channelLocator.getChannelAddress());
	}

	@Override
	public void hardwareAdded(HardwareDescriptor descriptor) {

		logger.debug("hardware added: " + descriptor.getIdentifier());

		String portName = null;

		if (descriptor.getHardwareType() == HardwareType.USB) {
			portName = ((UsbHardwareDescriptor) descriptor).getPortName();
		}
		else if (descriptor.getHardwareType() == HardwareType.SERIAL) {
			portName = ((SerialHardwareDescriptor) descriptor).getPortName();
		}
		else {
			logger.debug("error, descriptor has unsupported hardware type: " + descriptor.getHardwareType().toString());
		}

		if (null == portName) {
			logger.error("Add device with port name null.");
			return;
		}

		Connection con = new Connection(portName, this);
		addConnection(con);
	}

	@Override
	public void hardwareRemoved(HardwareDescriptor descriptor) {
		logger.debug("hardware removed: " + descriptor.getIdentifier().toString());
		String portName = null;

		if (descriptor.getHardwareType() == HardwareType.USB) {
			portName = ((UsbHardwareDescriptor) descriptor).getPortName();
		}
		else if (descriptor.getHardwareType() == HardwareType.SERIAL) {
			portName = ((SerialHardwareDescriptor) descriptor).getPortName();
		}

		Connection con = connectionsMap.get(portName);
		if (con != null)
			con.close();
		connectionsMap.remove(portName);
	}

	@Override
	public void shutdown() {
		Set<Entry<String, Connection>> connections = connectionsMap.entrySet();
		for (Entry<String, Connection> con : connections) {
			con.getValue().getLocalDevice().close();
		}
	}

	@Override
	public void addDeviceListener(DeviceListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeDeviceListener(DeviceListener listener) {
		// TODO Auto-generated method stub

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
