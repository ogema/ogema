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
package org.ogema.driver.dlms;

/**
 * This driver is used for communication with a smart meter via the dlms/cosem protocol, which in turn uses the
 * hdlc protocol. Only the readChannels() method is implemented currently, the writeChannels() has to be implemented
 * in future. To get a better impression of the channel addressing, there's a simple example at src/test/java attached.
 * For further information see the Kamstrup's manual(used for the tests) or the official OGEMA wiki, where the jdlms
 * library can be found.
 * 
 */

import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
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
import org.ogema.core.channelmanager.measurements.BooleanValue;
import org.ogema.core.channelmanager.measurements.DoubleValue;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.IntegerValue;
import org.ogema.core.channelmanager.measurements.LongValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.StringValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.openmuc.jdlms.client.ClientConnectionSettings;
import org.openmuc.jdlms.client.ClientConnectionSettings.Authentication;
import org.openmuc.jdlms.client.Data;
import org.openmuc.jdlms.client.Data.Choices;
import org.openmuc.jdlms.client.GetRequest;
import org.openmuc.jdlms.client.GetResult;
import org.openmuc.jdlms.client.IClientConnection;
import org.openmuc.jdlms.client.IClientConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true)
@Service(ChannelDriver.class)
public class DlmsDriver implements ChannelDriver {

	private final static Logger logger = LoggerFactory.getLogger(DlmsDriver.class);

	private final IClientConnectionFactory connectionFactory = new OsgiClientConnectionFactory();
	private final AddressParser addressParser = new AddressParser();

	private final static String ID = "dlms";
	private final static String DESCRIPTION = "This is a driver to communicate with smart meter over the IEC 62056 DLMS/COSEM protocol";

	private final List<DeviceConnection> connections = new ArrayList<DeviceConnection>();

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
		// TODO Auto-generated method stub

	}

	@Override
	public List<ChannelLocator> getChannelList(DeviceLocator device) throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void readChannels(List<SampledValueContainer> channels) throws UnsupportedOperationException, IOException {

		List<GetResult> getResults = null;

		for (SampledValueContainer container : channels) {

			ChannelLocator channelLocator = container.getChannelLocator();

			IClientConnection con = findConnection(channelLocator.getDeviceLocator().getDeviceAddress());
			DeviceConnection devCon = findDeviceConnection(channelLocator.getDeviceLocator().getDeviceAddress());
			ChannelLocator locator = devCon.findChannel(channelLocator);
			ChannelAddress channel = devCon.getChannelAddress(locator);

			GetRequest getParam = new GetRequest(channel.getClassId(), channel.getInstanceId(), channel
					.getAttributeId());

			try {

				// read data

				// TODO correct?
				// getResults = con.get(1000, false, getParam);
				getResults = con.get(2000, getParam);
				SampledValue sample = new SampledValue(getValue(getResults.get(0)), new Timestamp(new Date().getTime())
						.getTime(), Quality.GOOD);

				container.setSampledValue(sample);

			} catch (IOException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				throw new IOException("Unknown channel: " + channelLocator, e);
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
		// TODO Auto-generated method stub

	}

	@Override
	public void writeChannels(List<ValueContainer> channels) throws UnsupportedOperationException, IOException,
			NoSuchDeviceException, NoSuchChannelException {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeChannels(List<ValueContainer> channels, ExceptionListener listener)
			throws UnsupportedOperationException {
		// TODO Auto-generated method stub

	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

	@Override
	public void channelAdded(ChannelLocator channel) {

		DeviceLocator device = channel.getDeviceLocator();
		String devAdress = device.getDeviceAddress();

		SettingsHelper settingsHelper = new SettingsHelper(device.getParameters());

		IClientConnection connection;
		ChannelLocator channelLocator;
		DeviceConnection deviceConnection;

		// Connection handling
		connection = findConnection(devAdress);

		if (connection == null) {

			try {

				ClientConnectionSettings<?> params = addressParser.parse(device.getInterfaceName(), device
						.getDeviceAddress(), settingsHelper);
				connection = connectionFactory.createClientConnection(params);
				params.setAuthentication(Authentication.LOW);

			} catch (UnknownHostException uhEx) {
				try {
					throw new ConnectException("Device " + device.getDeviceAddress() + " not found");
				} catch (ConnectException e) {
					e.printStackTrace();
				}
			}

			catch (IOException ioEx) {
				try {
					throw new ConnectException("Cannot create connection object. Reason: " + ioEx);
				} catch (ConnectException e) {
					e.printStackTrace();
				}
			}

			logger.info("Connecting to device : " + device.getDeviceAddress());

			// Try to connect to device
			try {
				connection.connect(5000, settingsHelper.getPassword());

			} catch (IOException ex) {
				try {
					throw new ConnectException(ex.getMessage() + " BLABLABLA");
				} catch (ConnectException e) {
					e.printStackTrace();
				}
			}

			logger.debug("Connected to device: " + device.getDeviceAddress());

			ConnectionHandle handle = new ConnectionHandle(connection, settingsHelper);

			DeviceConnection devCon = new DeviceConnection(handle, devAdress);

			connections.add(devCon);

		}

		// Channel handling
		deviceConnection = findDeviceConnection(devAdress);

		channelLocator = deviceConnection.findChannel(channel);

		if (channelLocator == null) {

			String channelAdress = channel.getChannelAddress();

			String result[] = channelAdress.split(":");

			int classID = Integer.parseInt(result[0]);
			int attributeID = Integer.parseInt(result[2]);
			String instanceID = result[1];

			ChannelAddress channelAddress = new ChannelAddress(classID, instanceID, attributeID);

			deviceConnection.addChannelAddress(channelAddress);
			deviceConnection.addChannel(channel);

		}

	}

	@Override
	public void channelRemoved(ChannelLocator channel) {

		DeviceConnection devCon = findDeviceConnection(channel.getDeviceLocator().getDeviceAddress());
		ChannelAddress address = devCon.getChannelAddress(channel);
		// ConnectionHandle handle = (ConnectionHandle) devCon.getConnectionHandle();

		devCon.removeChannel(channel);
		devCon.removeChannelAddress(address);

		if (!devCon.hasChannels()) {
			connections.remove(devCon);
			// handle.getConnection().disconnect(handle.getSettings().sendDisconnect());
		}
	}

	private IClientConnection findConnection(String devAdress) {

		for (DeviceConnection con : connections) {

			if (con.getDeviceAddress().equals(devAdress)) {

				return con.getConnectionHandle().getConnection();
			}
		}

		return null;
	}

	private DeviceConnection findDeviceConnection(String devAdress) {

		for (DeviceConnection con : connections) {

			if (con.getDeviceAddress().equals(devAdress)) {

				return con;
			}
		}

		return null;
	}

	private Value getValue(GetResult result) {

		// TODO -> Check size of required datatypes
		Data data = result.getResultData();

		if (data.getChoiceIndex() == Choices.BOOL) {
			return new BooleanValue(data.getBoolean());
		}
		else if (data.getChoiceIndex() == Choices.INTEGER) {
			return new IntegerValue(data.getNumber().intValue());
		}
		else if (data.getChoiceIndex() == Choices.FLOAT32) {
			return new FloatValue(data.getNumber().floatValue());
		}
		else if (data.getChoiceIndex() == Choices.DOUBLE_LONG) {
			return new DoubleValue(data.getNumber().doubleValue());
		}
		else if (data.getChoiceIndex() == Choices.DOUBLE_LONG_UNSIGNED) {
			return new DoubleValue(data.getNumber().doubleValue());
		}
		else if (data.getChoiceIndex() == Choices.VISIBLE_STRING) {
			return new StringValue(new String(data.getByteArray()));
		}
		else if (data.getChoiceIndex() == Choices.LONG_INTEGER) {
			return new LongValue(data.getNumber().longValue());
		}
		else if (data.getChoiceIndex() == Choices.LONG_UNSIGNED) {
			return new LongValue(data.getNumber().longValue());
		}
		else if (data.getChoiceIndex() == Choices.LONG64) {
			return new LongValue(data.getNumber().longValue());
		}

		return null;

	}

}
