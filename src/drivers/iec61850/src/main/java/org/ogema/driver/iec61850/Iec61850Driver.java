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
package org.ogema.driver.iec61850;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
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
import org.ogema.core.channelmanager.driverspi.NoSuchInterfaceException;
import org.ogema.core.channelmanager.driverspi.SampledValueContainer;
import org.ogema.core.channelmanager.driverspi.ValueContainer;
import org.ogema.core.channelmanager.measurements.BooleanValue;
import org.ogema.core.channelmanager.measurements.ByteArrayValue;
import org.ogema.core.channelmanager.measurements.DoubleValue;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.IntegerValue;
import org.ogema.core.channelmanager.measurements.LongValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.openmuc.openiec61850.BasicDataAttribute;
import org.openmuc.openiec61850.BdaBitString;
import org.openmuc.openiec61850.BdaBoolean;
import org.openmuc.openiec61850.BdaFloat32;
import org.openmuc.openiec61850.BdaFloat64;
import org.openmuc.openiec61850.BdaInt16;
import org.openmuc.openiec61850.BdaInt32;
import org.openmuc.openiec61850.BdaInt64;
import org.openmuc.openiec61850.BdaInt8;
import org.openmuc.openiec61850.BdaTimestamp;
import org.openmuc.openiec61850.ClientAssociation;
import org.openmuc.openiec61850.ClientSap;
import org.openmuc.openiec61850.Fc;
import org.openmuc.openiec61850.ModelNode;
import org.openmuc.openiec61850.ServerModel;
import org.openmuc.openiec61850.ServiceError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = false)
@Service(ChannelDriver.class)
public class Iec61850Driver implements ChannelDriver {

	private static final String DRIVER_ID = "iec61850";
	private static final String DESCRIPTION = "This driver can be used to access devices using IEC 61850 (MMS)";

	private final static String deviceAddressSyntax = "url:port[:authenticationParameter]";

	private static Logger logger = LoggerFactory.getLogger(Iec61850Driver.class);

	private final HashMap<String, Iec61850Connection> connections = new HashMap<String, Iec61850Connection>();

	@Override
	public String getDriverId() {
		return DRIVER_ID;
	}

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	/**
	 * Scanning method that returns all connected devices.
	 */
	@Override
	public void startDeviceScan(String interfaceId, String filter, DeviceScanListener listener)
			throws UnsupportedOperationException, NoSuchInterfaceException, IOException {
		throw new UnsupportedOperationException();
	}

	/**
	 * Scanning method that returns all possible channels of a device.
	 */
	@Override
	public List<ChannelLocator> getChannelList(DeviceLocator device) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	/**
	 * SampledValueContainer has the ChannelLocator which in turn has the addressing. The addressing has to be string
	 * parsed in order to get which registers have to be read. The addressing has to be parsed once for every new
	 * ChannelLocator, if ChannelLocators are immutable. The serial port parameters are set according to the setting for
	 * each device.
	 */
	@Override
	public void readChannels(List<SampledValueContainer> channels) throws UnsupportedOperationException, IOException {
		System.out.println(channels.get(0).getChannelLocator().getDeviceLocator().getDeviceAddress());
		Iec61850Connection connectionHandle = connections.get(channels.get(0).getChannelLocator().getDeviceLocator()
				.getDeviceAddress());

		ServerModel serverModel = connectionHandle.getServerModel();

		for (SampledValueContainer container : channels) {

			ModelNode modelNode;
			String channelAddress = container.getChannelLocator().getChannelAddress();
			modelNode = serverModel.findModelNode(channelAddress.substring(0, channelAddress.length() - 3), Fc
					.fromString(channelAddress.substring(channelAddress.length() - 2, channelAddress.length())));

			if (modelNode == null) {
				logger
						.warn("Error reading channel: no FCDO/DA with object reference {} was not found in the server model."
								+ channelAddress);
				container.setSampledValue(new SampledValue(null, 0, Quality.BAD));
				continue;
			}

			BasicDataAttribute bda;
			try {
				bda = (BasicDataAttribute) modelNode;
			} catch (ClassCastException e) {
				logger
						.warn("Error reading channel: ModelNode with object reference {} was found in the server model but is not a Basic Data Attribute."
								+ channelAddress);
				container.setSampledValue(new SampledValue(null, 0, Quality.BAD));
				continue;
			}

			try {
				connectionHandle.getClientAssociation().getDataValues(bda);
			} catch (ServiceError e) {
				logger
						.warn("Error reading channel: service error calling getDataValues on {}: {}" + channelAddress
								+ e);
				container.setSampledValue(new SampledValue(null, 0, Quality.BAD));
				continue;
			}

			long receiveTime = System.currentTimeMillis();

			setRecord(container, bda, receiveTime);

		}

	}

	private void setRecord(SampledValueContainer container, BasicDataAttribute bda, long receiveTime) {

		switch (bda.getBasicType()) {
		case CHECK:
		case DOUBLE_BIT_POS:
		case OPTFLDS:
		case QUALITY:
		case REASON_FOR_INCLUSION:
		case TAP_COMMAND:
		case TRIGGER_CONDITIONS:
		case ENTRY_TIME:
		case OCTET_STRING:
		case VISIBLE_STRING:

		case UNICODE_STRING:
			container.setSampledValue(new SampledValue(new ByteArrayValue(((BdaBitString) bda).getValue()),
					receiveTime, Quality.GOOD));
			break;
		case TIMESTAMP:
			if (container.getChannelLocator().getChannelAddress().endsWith(":bytestring")) {
				container.setSampledValue(new SampledValue(new ByteArrayValue(((BdaBitString) bda).getValue()),
						receiveTime, Quality.GOOD));

			}
			else {
				container.setSampledValue(new SampledValue(new LongValue(((BdaTimestamp) bda).getDate().getTime()),
						receiveTime, Quality.GOOD));
			}
			break;
		case BOOLEAN:
			container.setSampledValue(new SampledValue(new BooleanValue(((BdaBoolean) bda).getValue()), receiveTime,
					Quality.GOOD));
			break;
		case FLOAT32:
			container.setSampledValue(new SampledValue(new FloatValue(((BdaFloat32) bda).getFloat()), receiveTime,
					Quality.GOOD));
			break;
		case FLOAT64:
			container.setSampledValue(new SampledValue(new DoubleValue(((BdaFloat64) bda).getDouble()), receiveTime,
					Quality.GOOD));
			break;
		case INT8:
			container.setSampledValue(new SampledValue(new IntegerValue(((BdaInt8) bda).getValue()), receiveTime,
					Quality.GOOD));

			break;
		case INT8U:
		case INT16:
			container.setSampledValue(new SampledValue(new IntegerValue(((BdaInt16) bda).getValue()), receiveTime,
					Quality.GOOD));

			break;
		case INT16U:
		case INT32:
			container.setSampledValue(new SampledValue(new IntegerValue(((BdaInt32) bda).getValue()), receiveTime,
					Quality.GOOD));

			break;
		case INT32U:
		case INT64:
			container.setSampledValue(new SampledValue(new LongValue(((BdaInt64) bda).getValue()), receiveTime,
					Quality.GOOD));

			break;
		default:
			throw new IllegalStateException("unknown BasicType received: " + bda.getBasicType());
		}
	}

	/**
	 * The port parameters are varied according to each accessed device. How do the synchronous read and the async read
	 * work together? (They should be a queue of transactions)
	 */
	@Override
	public void listenChannels(List<SampledValueContainer> channels, ChannelUpdateListener listener)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void writeChannels(List<ValueContainer> channels) throws UnsupportedOperationException, IOException {
		throw new UnsupportedOperationException();
	}

	/**
	 * Frees all channels, devices and interfaces
	 */
	@Override
	public void reset() {
		for (Iec61850Connection con : connections.values()) {
			con.getClientAssociation().close();
		}
		connections.clear();
	}

	@Override
	public void channelAdded(ChannelLocator channel) {
		String deviceAddress = channel.getDeviceLocator().getDeviceAddress();

		Iec61850Connection connection = connections.get(deviceAddress);

		if (connection != null) {
			return;
		}

		String[] deviceAddresses = deviceAddress.split(":");

		if (deviceAddresses.length < 2 || deviceAddresses.length > 3) {
			throw new IllegalStateException("invalid device address syntax, correct syntax: " + deviceAddressSyntax);
		}

		String remoteHost = deviceAddresses[0];
		InetAddress address;
		try {
			address = InetAddress.getByName(remoteHost);
		} catch (UnknownHostException e) {
			throw new IllegalStateException("Unknown host: " + remoteHost, e);
		}

		int remotePort;
		try {
			remotePort = Integer.parseInt(deviceAddresses[1]);
		} catch (NumberFormatException e) {
			throw new IllegalStateException("invalid port specified", e);
		}

		ClientSap clientSap = new ClientSap();

		String settings = channel.getDeviceLocator().getParameters();

		if (settings != null && !settings.isEmpty()) {
			String[] splitSettings = settings.split(";");
			for (String setting : splitSettings) {
				String[] splitSetting = setting.split("=");
				if (splitSetting.length < 1 || splitSetting.length > 2) {
					throw new IllegalStateException("settings syntax incorrect");
				}
				if (splitSetting[0].equalsIgnoreCase("TSelLocal")) {
					if (splitSetting.length == 1) {
						clientSap.setTSelLocal(null);
					}
					else {
						byte[] tSelLocal = new byte[splitSetting[1].length()];
						for (int i = 0; i < splitSetting[1].length(); i++) {
							tSelLocal[i] = (byte) splitSetting[1].charAt(i);
						}
						clientSap.setTSelLocal(tSelLocal);
					}
				}
				else if (splitSetting[0].equalsIgnoreCase("TSelRemote")) {
					if (splitSetting.length == 1) {
						clientSap.setTSelRemote(null);
					}
					else {
						byte[] tSelRemote = new byte[splitSetting[1].length()];
						for (int i = 0; i < splitSetting[1].length(); i++) {
							tSelRemote[i] = (byte) splitSetting[1].charAt(i);
						}
						clientSap.setTSelRemote(tSelRemote);
					}
				}
			}
		}

		ClientAssociation clientAssociation;
		try {
			clientAssociation = clientSap.associate(address, remotePort, null, null);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}

		ServerModel serverModel;
		try {
			serverModel = clientAssociation.retrieveModel();
		} catch (ServiceError e) {
			clientAssociation.close();
			throw new IllegalStateException("Service error retrieving server model" + e.getMessage(), e);
		} catch (IOException e) {
			clientAssociation.close();
			throw new IllegalStateException("IOException retrieving server model: " + e.getMessage(), e);
		}
		connections.put(deviceAddress, new Iec61850Connection(clientAssociation, serverModel));
	}

	@Override
	public void channelRemoved(ChannelLocator channel) {

	}

	@Override
	public void abortDeviceScan(String interfaceId, String filter) {
		// ignore since device scan is not supported!
	}

	@Override
	public void startChannelScan(DeviceLocator device, ChannelScanListener listener)
			throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public void readChannels(List<SampledValueContainer> channels, ChannelUpdateListener listener)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void writeChannels(List<ValueContainer> channels, ExceptionListener listener)
			throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

}
