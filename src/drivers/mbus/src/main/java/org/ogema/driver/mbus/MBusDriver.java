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
package org.ogema.driver.mbus;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.apache.felix.scr.annotations.Component;
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
import org.ogema.core.channelmanager.measurements.DoubleValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.openmuc.jmbus.DataRecord;
import org.openmuc.jmbus.DecodingException;
import org.openmuc.jmbus.MBusSap;
import org.openmuc.jmbus.VariableDataStructure; //import org.openmuc.jmbus.VariableDataBlock;
//import org.openmuc.jmbus.VariableDataResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = false)
@Service(ChannelDriver.class)
public class MBusDriver implements ChannelDriver {
	private final static Logger logger = LoggerFactory.getLogger(MBusDriver.class);

	private final Map<String, ConnectionHandle> connections = new HashMap<String, ConnectionHandle>();

	private final static String ID = "mbus";
	private final static String DESCRIPTION = "This is a driver to communicate with M-Bus Devices";

	@Override
	public String getDriverId() {
		return ID;
	}

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	@Override
	public void startDeviceScan(String interfaceId, String filter, DeviceScanListener listener)
			throws UnsupportedOperationException, NoSuchInterfaceException, IOException {

		throw new UnsupportedOperationException();

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
		throw new UnsupportedOperationException();
	}

	@Override
	public synchronized void readChannels(List<SampledValueContainer> channels)
			throws UnsupportedOperationException, IOException {

		long timestamp = System.currentTimeMillis();

		for (SampledValueContainer connection : channels) {

			DeviceLocator deviceLocator = connection.getChannelLocator().getDeviceLocator();
			ConnectionHandle connectionHandle = connections.get(deviceLocator.getInterfaceName());
			MBusSap mBusSap = connectionHandle.getMBusSap();

			VariableDataStructure response = null;
			try {

				if (!connectionHandle.isConnected()) {
					logger.info("********* called open for: " + deviceLocator.getInterfaceName());
					mBusSap.open();
					connectionHandle.setConnected(true);
				}

				response = mBusSap.read(new Integer(
						deviceLocator.getDeviceAddress().substring(1, deviceLocator.getDeviceAddress().length())));

				if (response != null) {
					try {
						response.decode();
						DoubleValue scaledValue = getScaledValue(response, connection);
						connection.setSampledValue(new SampledValue(scaledValue, timestamp, Quality.GOOD));
					} catch (DecodingException e) {
						logger.error("Couldn't decode mbus resopnse.", e);
						connection.setSampledValue(new SampledValue(new DoubleValue(0), timestamp, Quality.BAD));
					}
				}
				else {
					throw new IOException("read response = null");
				}

			} catch (IOException | TimeoutException e) {
				logger.info("run into exception for device:" + deviceLocator.getInterfaceName(), e);
				connection.setSampledValue(new SampledValue(new DoubleValue(0), timestamp, Quality.BAD));
				connectionHandle.setConnected(false);
				connectionHandle.getMBusSap().close();

			}

		}

	}

	private DoubleValue getScaledValue(VariableDataStructure response, SampledValueContainer connection)
			throws DecodingException {

		List<DataRecord> vdbs = response.getDataRecords();
		String[] dibvibs = new String[vdbs.size()];
		int i = 0;

		DoubleValue returnValue = null;

		for (DataRecord vdb : vdbs) {
			dibvibs[i++] = bytesToHexString(vdb.getDIB()) + ':' + bytesToHexString(vdb.getVIB());
		}

		i = 0;
		for (DataRecord vdb : response.getDataRecords()) {
			if (dibvibs[i++].equalsIgnoreCase(connection.getChannelLocator().getChannelAddress())) {
				vdb.decode();
				returnValue = new DoubleValue(vdb.getScaledDataValue());
			}
		}

		return returnValue;
	}

	@Override
	public void listenChannels(List<SampledValueContainer> channels, ChannelUpdateListener listener)
			throws UnsupportedOperationException, NoSuchDeviceException, NoSuchChannelException, IOException {
		throw new UnsupportedOperationException();

	}

	@Override
	public void writeChannels(List<ValueContainer> channels) throws UnsupportedOperationException, IOException,
			NoSuchDeviceException, NoSuchChannelException {
		throw new UnsupportedOperationException();

	}

	@Override
	public void shutdown() {

		/*
		 * Iterator<Map.Entry<String, ConnectionHandle>> it = connections.entrySet().iterator();
		 * 
		 * while (it.hasNext()) { String key = it.next().getKey(); ConnectionHandle handle = connections.get(key);
		 * handle.getMBusSap().close();
		 * 
		 * } connections.clear();
		 */
	}

	@Override
	public void channelAdded(ChannelLocator channel) {
		DeviceLocator deviceLocator = channel.getDeviceLocator();
		ConnectionHandle connectionHandle = connections.get(channel.getDeviceLocator().getInterfaceName());
		if (connectionHandle == null) {
			MBusSap mBusSap = new MBusSap(deviceLocator.getInterfaceName(), new Integer(deviceLocator.getParameters()));
			connectionHandle = new ConnectionHandle(mBusSap, deviceLocator.getDeviceAddress());
			connections.put(deviceLocator.getInterfaceName(), connectionHandle);
		}

	}

	@Override
	public void channelRemoved(ChannelLocator channel) {
		// TODO Auto-generated method stub
		ConnectionHandle connectionHandle = connections.get(channel.getDeviceLocator().getInterfaceName());
		if (!connectionHandle.isConnected()) {
			return;
		}
		connectionHandle.decreaseDeviceCounter();
		if (connectionHandle.getDeviceCounter() == 0) {
			connectionHandle.getMBusSap().close();
			connections.remove(channel.getDeviceLocator().getInterfaceName());
		}

	}

	private String bytesToHexString(byte[] bytes) {

		StringBuilder sb = new StringBuilder();
		for (byte b : bytes) {
			sb.append(String.format("%1$02X", b));
		}
		return sb.toString();
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
		throw new UnsupportedOperationException();
	}

}
