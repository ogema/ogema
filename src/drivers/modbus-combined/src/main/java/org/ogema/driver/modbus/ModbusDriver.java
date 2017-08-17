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
package org.ogema.driver.modbus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
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
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.hardwaremanager.HardwareDescriptor;
import org.ogema.core.hardwaremanager.HardwareDescriptor.HardwareType;
import org.ogema.core.hardwaremanager.HardwareListener;
import org.ogema.core.hardwaremanager.HardwareManager;
import org.ogema.core.hardwaremanager.UsbHardwareDescriptor;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a wrapper for the jamod api (http://jamod.sourceforge.net)
 * 
 * On every connectDevice() a new Modbus connection is created. The Modbus connection is torn down on disconnectDevice()
 * 
 * This driver offers Modbus RTU/TCP master transfers.
 * 
 * The following channel addressing modes are supported by this driver:
 * 
 * - access 16bit register(s) called Input Registers (read only) 
 *   channelAddressString format: "<DEVICE_ID>:INPUT_REGISTERS:<REGISTERNUMBER>:<COUNT>"
 * 
 * - access 16bit register(s) called Holding Registers (read / write) 
 *   channelAddressString format: "<DEVICE_ID>:HOLDING_REGISTERS:<REGISTERNUMBER>:<COUNT>"
 * 
 * - access 1bit called Discrete Input (read only) 
 *   channelAddressString format: "<DEVICE_ID>:DISCRETE_INPUTS:<REGISTERNUMBER>:<COUNT>"
 * 
 * - access 1bit called Coils (read / write) 
 *   channelAddressString format: "<DEVICE_ID>:COILS:<REGISTERNUMBER>:<COUNT>"
 * 
 * The parts of the addressString are:
 * 
 * - Transfer type: which modbus function is used
 * INPUT_REGISTERS read : function code 4 - Read Input Registers
 * INPUT_REGISTERS write : read only!
 * 
 * HOLDING_REGISTERS read : function code 3 - read holding registers
 * HOLDING_REGISTERS write : function code 16 - write multiple holding registers
 * 
 * DISCRETE_INPUTS read : function code 2 - read discrete inputs
 * DISCRETE_INPUTS write : read only!
 * 
 * COILS read : function code 1 - read coils
 * COILS write : function code 15 - write multiple coils
 * 
 * - Device Id: modbus device address (normally 0 for modbus/tcp)
 * 
 * - Registernumber : address of first addressed register (0-65535)
 * e.g. read holding register 0 -> modbus register address 400001
 * 
 * - Count : number of addressed coils/inputs/etc
 * e.g. read holding register 0 count 5 -> modbus registers 400001 - 400005
 * 
 * - Datatype
 * all read from a channel is an int[] array. Boolean values (COILS or DISCRETE_INPUTS) are converted to in (false -> 0, true -> 1).
 * all data written to a channel must be an int[] array. Boolean values are interpreted (0 -> false, !0 -> true)
 * 
 * 
 * @author pau
 * 
 */
@Component
@Service(ChannelDriver.class)
public class ModbusDriver implements ChannelDriver, HardwareListener {

	private static final String DRIVER_ID = "modbus-combined";
	private static final String DESCRIPTION = "MODBUS/RTU or MODBUS/TCP";

	final static Logger logger = LoggerFactory.getLogger(ModbusDriver.class);

	private final List<Connection> connectionList;

	private Set<DeviceListener> deviceListeners = new HashSet<DeviceListener>();
	
	@Reference
	private HardwareManager hardwareManager;

	public ModbusDriver() {
		connectionList = new ArrayList<Connection>();
	}

	@Activate
	protected void activate(BundleContext ctx) {
		hardwareManager.addListener(this);
	}

	@Deactivate
	protected void deactivate(BundleContext ctx) {
		hardwareManager.removeListener(this);
	}

	@Override
	public String getDriverId() {
		return DRIVER_ID;
	}

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	/**
	 * Scanning method that returns all connected devices - not applicable for
	 * Modbus
	 */
	@Override
	public void startDeviceScan(String interfaceId, String filter,
			DeviceScanListener listener) throws UnsupportedOperationException,
			NoSuchInterfaceException, IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void abortDeviceScan(String interfaceId, String filter) {
		// ignore since device scan is not supported
	}

	/**
	 * Scanning method that returns all channels of a device - not applicable
	 * for Modbus
	 */
	@Override
	public void startChannelScan(DeviceLocator device,
			ChannelScanListener listener) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	/**
	 * Method that returns all current channels of a device. Does not connect to
	 * the device.
	 */
	@Override
	public List<ChannelLocator> getChannelList(DeviceLocator device)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	/**
	 * read all channels contained in the list. If a channel is not known,
	 * create it. If either a channel cannot be created or read, print log entry
	 * and throw IOException.
	 */
	@Override
	public void readChannels(List<SampledValueContainer> channels)
			throws UnsupportedOperationException, IOException {

		if (channels == null)
			return;
		
		for (SampledValueContainer container : channels) {
			readChannel(container);
		}
	}

	void readChannel(SampledValueContainer svc) throws IOException {
		ChannelLocator channelLocator;
		DeviceLocator deviceLocator;
		SampledValue result;
		Connection con;
		Channel channel;

		if (svc == null)
			return;

		channelLocator = svc.getChannelLocator();
		
		if (channelLocator == null)
			throw new IOException("malformed SampledValueContainer. getChannelLocator() == null");
		
		deviceLocator = channelLocator.getDeviceLocator();
		
		if (deviceLocator == null)
			throw new IOException("malformed channelLocator. getDeviceLocator() == null");
		
		try {
			con = getConnection(deviceLocator);
			channel = con.getChannel(channelLocator);

			// read data
			result = channel.readValue(con);

		} catch (IOException e) {
			logger.error(String.format("could not read channel {} because of io errors", channelLocator), e);
			result = new SampledValue(null, System.currentTimeMillis(), Quality.BAD);
		} catch (NullPointerException e) {
			// log message already printed
			result = new SampledValue(null, System.currentTimeMillis(),
					Quality.BAD);
		}

		svc.setSampledValue(result);		
	}
	
	private synchronized Connection getConnection(DeviceLocator deviceLocator) throws IOException {

		Connection connection = findConnection(deviceLocator);

		if (connection == null) {
				connection = new Connection(this, deviceLocator);
				addConnection(connection);
		}

		return connection;
	}

	@Override
	public void listenChannels(List<SampledValueContainer> channels,
			ChannelUpdateListener listener)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void writeChannels(List<ValueContainer> channels)
			throws UnsupportedOperationException, IOException,
			NoSuchDeviceException, NoSuchChannelException {
		for (ValueContainer container : channels) {
			writeChannel(container.getChannelLocator(), container.getValue());
		}
	}

	/**
	 * Frees all channels, devices and interfaces
	 */
	@Override
	public void shutdown() {
		for (Connection con : connectionList) {
			removeConnection(con);
			con.close();
		}
	}

	@Override
	public void channelAdded(ChannelLocator channel) {
	}

	@Override
	synchronized public void channelRemoved(ChannelLocator channel) {

		DeviceLocator device = channel.getDeviceLocator();

		Connection con;

		con = findConnection(device);

		if (con != null)
		{
			con.removeChannel(channel);

			if (!con.hasChannels()) {
				removeConnection(con);
				con.close();
			}
		}
	}

	private void removeConnection(Connection con) {
		connectionList.remove(con);
	}

	private void addConnection(Connection con) {
		connectionList.add(con);
	}

	private Connection findConnection(DeviceLocator locator) {
		for (Connection con : connectionList) {
			if (con.getDeviceLocator().equals(locator))
				return con;
		}

		return null;
	}

	@Override
	public void hardwareAdded(HardwareDescriptor descriptor) {
	}

	@Override
	synchronized public void hardwareRemoved(HardwareDescriptor descriptor) {
		String name1;
		String name2;

		// only USB serials can be removed!
		if (descriptor.getHardwareType() == HardwareType.USB) {
			name2 = ((UsbHardwareDescriptor) descriptor).getPortName();

			for (Connection con : connectionList) {
				name1 = con.getDeviceLocator().getInterfaceName();

				if (name1.equals(name2)) {
					connectionList.remove(con);
					con.close();
					callDeviceListeners(con.getDeviceLocator());
					break;
				}
			}
		}
	}

	HardwareManager getHardwareManager() {
		return hardwareManager;
	}


	@Override
	public void addDeviceListener(DeviceListener listener) {
		deviceListeners.add(listener);
	}

	@Override
	public void removeDeviceListener(DeviceListener listener) {
		deviceListeners.remove(listener);
	}

	private void callDeviceListeners(DeviceLocator device) {
		for(DeviceListener listener : deviceListeners)
		{
			try {
				listener.deviceRemoved(device);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}
	
	@Override
	public void writeChannel(ChannelLocator channelLocator, Value value)
			throws UnsupportedOperationException, IOException,
			NoSuchDeviceException, NoSuchChannelException {
		try {
			Connection con;
			Channel channel;

			// returns valid connection or throws exception
			con = getConnection(channelLocator.getDeviceLocator());

			// returns valid channel or throws exception
			channel = con.getChannel(channelLocator);

			// write data
			channel.writeValue(con, value);

		} catch (NullPointerException e) {
			throw new IOException("Unknown channel: " + channelLocator, e);
		}
	}

}
