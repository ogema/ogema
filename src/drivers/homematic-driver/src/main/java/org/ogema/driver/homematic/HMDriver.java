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
package org.ogema.driver.homematic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.ogema.driver.homematic.manager.RemoteDevice;
import org.ogema.driver.homematic.manager.RemoteDevice.InitStates;
import org.slf4j.Logger;

public class HMDriver implements ChannelDriver, HardwareListener {

	public final static Logger logger = org.slf4j.LoggerFactory.getLogger("homematic-driver");

	private final Map<String, Connection> connectionsMap; // <interfaceId, connection>
	private final String driverId = "homematic-driver";
	private final String description = "Ogema Homematic Driver";
	private final Map<ChannelUpdateListener, List<Channel>> listenerMap;

	private final Object connectionLock = new Object();

	public HMDriver() {
		connectionsMap = new HashMap<>();
		listenerMap = new HashMap<>();
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
	 * Scan for connected devices NOT available devices.
	 */
	@Override
	public void startDeviceScan(String interfaceId, String filter, DeviceScanListener listener)
			throws UnsupportedOperationException, NoSuchInterfaceException, IOException {
		boolean success = false;
		Connection connection = connectionsMap.get(interfaceId);
		if (connection != null) {
			for (Map.Entry<String, RemoteDevice> deviceEntry : connection.localDevice.getDevices().entrySet()) {
				RemoteDevice remoteDevice = deviceEntry.getValue();
				if (remoteDevice.getInitState() == InitStates.PAIRED) {
					String parameters = remoteDevice.getDeviceType();
					DeviceLocator deviceLocator = new DeviceLocator(driverId, interfaceId, remoteDevice.getAddress(),
							parameters);
					logger.debug("\nDevice found:\nAddress = " + deviceLocator.getDeviceAddress() + "\nDeviceType = "
							+ deviceLocator.getParameters());
					listener.deviceFound(deviceLocator);
					success = true;
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
	public void startChannelScan(DeviceLocator device, ChannelScanListener listener)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException(); // TODO
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
				// in case of configuration channel we get null
				if (channel != null) {
					// read data
					container.setSampledValue(channel.readValue(con));
				}
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
				Channel channel = findConnection(channelLocator.getDeviceLocator().getInterfaceName())
						.findDevice(channelLocator.getDeviceLocator()).findChannel(channelLocator);
				channel.setEventListener(container, listener);
				channelList.add(channel);

			} catch (NullPointerException e) {
				throw new IOException("Unknown channel: " + channelLocator, e);
			}
		}
		listenerMap.put(listener, channelList);
	}

	@Override
	public void writeChannels(List<ValueContainer> channels)
			throws UnsupportedOperationException, IOException, NoSuchDeviceException, NoSuchChannelException {
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
	public void shutdown() {
		// TODO Auto-generated method stub

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
			chan = Channel.createChannel(channel, dev);
			dev.addChannel(chan);
		}
	}

	@Override
	public void channelRemoved(ChannelLocator channel) {
		DeviceLocator device = channel.getDeviceLocator();
		if (device == null)
			return;
		Connection con = findConnection(device.getInterfaceName());
		if (con == null)
			return;
		Device dev = con.findDevice(device);
		if (dev == null)
			return;
		Channel chan = null;
		chan = dev.findChannel(channel);
		if (chan != null)
			dev.removeChannel(chan);
	}

	@Override
	public void hardwareAdded(HardwareDescriptor descriptor) {
		/* This function is responsible for what to do when new hardware(usb stick) is added */
		logger.debug("hardware added: " + descriptor.getIdentifier().toString());
	}

	@Override
	public void hardwareRemoved(HardwareDescriptor descriptor) {

		/*
		 * What to do when stick is removed: ChannelManager has to support Device removed callbacks to hand this
		 * situation properly.
		 */
		logger.debug("hardware removed: " + descriptor.getIdentifier().toString());
	}

	volatile Thread pairing = null;

	public void enablePairing(final String iface) {
		pairing = new Thread() {
			@Override
			public void run() {
				try {
					// TODO: dirty connection
					Connection connection = findConnection(iface);
					connection.localDevice.setPairing("0000000000");
					logger.info("enabled Pairing for 60 seconds");
					Thread.sleep(60000);
					connection.localDevice.setPairing(null);
					logger.info("Pairing disabled.");
				} catch (InterruptedException e) {
					// e.printStackTrace();
				}
			}
		};
		pairing.setName("homematic-ll-enablePairing");
		pairing.start();

	}

	@Override
	public void addDeviceListener(DeviceListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeDeviceListener(DeviceListener listener) {

	}

	volatile Thread connectThread;

	public void establishConnection() {
		final String portname = Connection.getPortName();
		Connection con = connectionsMap.get(portname);
		if (con == null) {
			final HMDriver driver = this;
			// FIXME this thread stays alive after a framework shutdown
			connectThread = new Thread() {
				@Override
				public void run() {

					Connection con = new Connection(connectionLock, portname, "HMUSB");
					synchronized (connectionLock) {
						while (!con.hasConnection() && Activator.bundleIsRunning) {
							try {
								connectionLock.wait();
							} catch (InterruptedException ex) { // interrupt is used to terminate the thread
								// ex.printStackTrace();
							}
						}
					}
					if (!Activator.bundleIsRunning)
						return;
					addConnection(con);
					driver.enablePairing("USB");
				}
			};
			connectThread.setName("homematic-ll-connect");
			connectThread.start();
		}
		else {
			con.getLocalDevice().restart();
		}
	}

	@Override
	public void writeChannel(ChannelLocator channelLocator, Value value)
			throws UnsupportedOperationException, IOException, NoSuchDeviceException, NoSuchChannelException {
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
