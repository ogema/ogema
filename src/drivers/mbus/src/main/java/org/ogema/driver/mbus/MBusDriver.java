/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
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
import java.net.ConnectException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.channelmanager.ChannelAccess;
import org.ogema.core.channelmanager.driverspi.ChannelDriver;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.ChannelScanListener;
import org.ogema.core.channelmanager.driverspi.ChannelUpdateListener;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.channelmanager.driverspi.DeviceScanListener;
import org.ogema.core.channelmanager.driverspi.ExceptionListener;
import org.ogema.core.channelmanager.driverspi.NoSuchChannelException;
import org.ogema.core.channelmanager.driverspi.NoSuchDeviceException;
import org.ogema.core.channelmanager.driverspi.NoSuchInterfaceException;
import org.ogema.core.channelmanager.driverspi.SampledValueContainer;
import org.ogema.core.channelmanager.driverspi.ValueContainer;
import org.ogema.core.channelmanager.measurements.DoubleValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.openmuc.jmbus.DecodingException;
import org.openmuc.jmbus.MBusSap;
import org.openmuc.jmbus.VariableDataBlock;
import org.openmuc.jmbus.VariableDataResponse;
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
	public void readChannels(List<SampledValueContainer> channels) throws UnsupportedOperationException, IOException {

		for (SampledValueContainer connection : channels) {
			ConnectionHandle connectionHandle = connections.get(connection.getChannelLocator().getDeviceLocator()
					.getInterfaceName());

			MBusSap mBusSap = connectionHandle.getMBusSap();

			VariableDataResponse response = null;
			try {
				response = mBusSap.read(connection.getChannelLocator().getDeviceLocator().getDeviceAddress());
			} catch (IOException e1) {
				connectionHandle.close();
				connectionHandle.getMBusSap().close();
				connections.remove(connection.getChannelLocator().getDeviceLocator().getInterfaceName());
			} catch (TimeoutException e1) {

			}

			long timestamp = System.currentTimeMillis();

			List<VariableDataBlock> vdbs = response.getVariableDataBlocks();
			String[] dibvibs = new String[vdbs.size()];

			int i = 0;
			for (VariableDataBlock vdb : vdbs) {
				dibvibs[i++] = bytesToHexString(vdb.getDIB()) + ':' + bytesToHexString(vdb.getVIB());
			}
			i = 0;
			for (VariableDataBlock vdb : response.getVariableDataBlocks()) {

				if (dibvibs[i++].equalsIgnoreCase(connection.getChannelLocator().getChannelAddress())) {

					try {
						vdb.decode();
					} catch (DecodingException e) {
						logger.debug("Unable to parse VariableDataBlock received via M-Bus", e);
						break;
					}
					connection.setSampledValue(new SampledValue(new DoubleValue(vdb.getScaledDataValue()), timestamp,
							Quality.GOOD));
					break;

				}

			}

			if (connection.getSampledValue() == null) {
				System.out.println("YOU FAILD HARD!!!!");
			}

		}

	}

	@Override
	public void readChannels(List<SampledValueContainer> channels, ChannelUpdateListener listener)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
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
	public void writeChannels(List<ValueContainer> channels, ExceptionListener listener)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException();

	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

	@Override
	public void channelAdded(ChannelLocator channel) {

		ConnectionHandle connectionHandle = connections.get(channel.getDeviceLocator().getInterfaceName());

		if (connectionHandle == null) {
			MBusSap mBusSap = new MBusSap(channel.getDeviceLocator().getInterfaceName());
			try {
				mBusSap.open(Integer.parseInt(channel.getDeviceLocator().getParameters()));
			} catch (IOException e1) {
				e1.printStackTrace();
				try {
					throw new ConnectException("Unable to bind local interface: "
							+ channel.getDeviceLocator().getInterfaceName());
				} catch (ConnectException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (IllegalArgumentException e) {
				try {
					throw new Exception();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}

			try {
				mBusSap.read(channel.getDeviceLocator().getDeviceAddress());
			} catch (Exception e) {
				e.printStackTrace();
				mBusSap.close();
				try {
					throw new Exception(e);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}

			connectionHandle = new ConnectionHandle(mBusSap, channel.getDeviceLocator().getDeviceAddress());
			connections.put(channel.getDeviceLocator().getInterfaceName(), connectionHandle);

		}
		else {
			connectionHandle.increaseDeviceCounter();
		}
	}

	@Override
	public void channelRemoved(ChannelLocator channel) {
		// TODO Auto-generated method stub
		ConnectionHandle connectionHandle = connections.get(channel.getDeviceLocator().getInterfaceName());
		if (!connectionHandle.isOpen()) {
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

}
