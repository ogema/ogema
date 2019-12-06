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
package org.ogema.driver.wmbus;

import static javax.xml.bind.DatatypeConverter.printHexBinary;

import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.felix.scr.annotations.Component;
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
import org.ogema.core.channelmanager.measurements.DoubleValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.hardwaremanager.HardwareDescriptor;
import org.ogema.core.hardwaremanager.HardwareListener;
import org.ogema.core.hardwaremanager.HardwareManager;
import org.openmuc.jmbus.DataRecord;
import org.openmuc.jmbus.DecodingException;
import org.openmuc.jmbus.SecondaryAddress;
import org.openmuc.jmbus.VariableDataStructure;
import org.openmuc.jmbus.wireless.WMBusConnection;
import org.openmuc.jmbus.wireless.WMBusConnection.WMBusManufacturer;
import org.openmuc.jmbus.wireless.WMBusConnection.WMBusSerialBuilder;
import org.openmuc.jmbus.wireless.WMBusListener;
import org.openmuc.jmbus.wireless.WMBusMessage;
import org.openmuc.jmbus.wireless.WMBusMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(specVersion = "1.2", immediate = true)
@Service(ChannelDriver.class)
public class WirelessMBusDriver implements ChannelDriver, WMBusListener {
	private final static Logger logger = LoggerFactory.getLogger(WirelessMBusDriver.class);

	private static final String ID = "wmbus";
	private static final String DESCRIPTION = "This is a driver to communicate with wireless M-Bus Devices";
	private final Map<String, ConnectionHandle> connections = new HashMap<>();
	private final Set<ConnectionHandle> tranciverList = new HashSet<>();

	private List<SampledValueContainer> sampledValueContainers = new ArrayList<>();
	@Reference
	private HardwareManager hardwareManager;

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
			throws NoSuchInterfaceException, IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void abortDeviceScan(String interfaceId, String filter) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<ChannelLocator> getChannelList(DeviceLocator device) {
		return connections.get(device).getChannels(device);
	}

	@Override
	public void readChannels(List<SampledValueContainer> channels) throws IOException {
		throw new UnsupportedOperationException("Synchone Comunikation (Mode T) is not supportet yet");
	}

	@Override
	public void listenChannels(List<SampledValueContainer> channels, ChannelUpdateListener listener)
			throws NoSuchDeviceException, NoSuchChannelException, IOException {

		sampledValueContainers = channels;

		for (SampledValueContainer channel : channels) {
			ConnectionHandle handle = connections
					.get(channel.getChannelLocator().getDeviceLocator().getInterfaceName());
			if (handle == null) {
				throw new NoSuchDeviceException(channel.getChannelLocator().getDeviceLocator());
			}

			handle.setListener(listener);
		}
	}

	private byte[] getByteArrayFromShortHexString(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	public static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	@Override
	public void writeChannels(List<ValueContainer> channels)
			throws UnsupportedOperationException, IOException, NoSuchDeviceException, NoSuchChannelException {
		throw new UnsupportedOperationException();
	}

	@Override
	@org.osgi.service.component.annotations.Deactivate
	public void shutdown() {
		connections.forEach((k, v) -> {
			try {
				v.getMBusConnection().close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	public static String substringAfter(String string, String delimiter) {
		int pos = string.indexOf(delimiter);

		return pos >= 0 ? string.substring(pos + delimiter.length()) : "";
	}

	public static String substringBefore(String string, String delimiter) {
		int pos = string.indexOf(delimiter);

		return pos >= 0 ? string.substring(0, pos) : string;
	}

	@Override
	public void channelAdded(ChannelLocator channel) {
		DeviceLocator dl = channel.getDeviceLocator();
		String port = dl.getInterfaceName();
		ConnectionHandle ch = connections.get(port);
		if (ch == null) {
			try {
				establishConnection(channel);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		ch = connections.get(port);
		ch.addChannel(dl, channel);
	}

	public void establishConnection(ChannelLocator channel) throws IOException {
		String iface = channel.getDeviceLocator().getInterfaceName();
		String portName = null;
		if (iface.startsWith("HWM(")) {
			String descriptor = iface.substring(4, iface.length() - 1);
			portName = hardwareManager.getPortName("wmbus.iface.name", descriptor, null);
		}
		else {
			portName = iface.substring(0, iface.indexOf('!'));
		}
		ConnectionHandle con = connections.get(iface);
		if (con == null && portName != null) {
			WMBusManufacturer wmBusManufacturer = WMBusManufacturer.IMST;
			WMBusListener wmbusListener = this;
			WMBusSerialBuilder builder = new WMBusSerialBuilder(wmBusManufacturer, wmbusListener, portName)
					.setMode(WMBusMode.C);
			builder.setTimeout(3000);

			WMBusConnection wmBusConnection = builder.build();
			con = new ConnectionHandle(wmBusConnection, channel.getDeviceLocator());
			// con.open();
			connections.put(iface, con);
			tranciverList.add(con);
			DeviceLocator dl = channel.getDeviceLocator();
			con.addDevice(dl);
		}
		else {
			logger.info("No portname could be determined!");
		}
	}

	@Override
	public void channelRemoved(ChannelLocator channel) {
		DeviceLocator dl = channel.getDeviceLocator();
		ConnectionHandle handle = connections.get(dl.getInterfaceName());
		if (handle == null) {
			return;
		}
		List<ChannelLocator> cll = handle.getChannels(dl);
		cll.remove(channel);

		if (cll.isEmpty()) {
			handle.removeDevice(dl);

			// handle.decreaseDeviceCounter();
		}
		if (handle.getDeviceCount() == 0) {
			try {
				handle.getMBusConnection().close();
			} catch (IOException e) {
				e.printStackTrace();
			}
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

	public void servListeners(VariableDataStructure vd, SecondaryAddress sa) {
		long timestamp = System.currentTimeMillis();
		List<DataRecord> vdbs = vd.getDataRecords();
		String[] dibvibs = new String[vdbs.size()];

		int i = 0;
		for (DataRecord vdb : vdbs) {
			dibvibs[i++] = bytesToHexString(vdb.getDib()) + ':' + bytesToHexString(vdb.getVib());
		}
		i = 0;
		List<SampledValueContainer> list = new LinkedList<SampledValueContainer>();
		for (ConnectionHandle connectionHandle : tranciverList) {

			for (DataRecord vdb : vd.getDataRecords()) {

				for (SampledValueContainer sampledValueContainer : sampledValueContainers) {
					String channelAddress = sampledValueContainer.getChannelLocator().getChannelAddress();
					String deviceAddress = sampledValueContainer.getChannelLocator().getDeviceLocator()
							.getDeviceAddress();
					String deviceAddressFromMessage = getShortHexStringFromByteArray(sa.asByteArray());

					if (channelAddress.equals(dibvibs[i]) && deviceAddress.equals(deviceAddressFromMessage)) {

						SampledValue value = new SampledValue(new DoubleValue(vdb.getScaledDataValue()), timestamp,
								Quality.GOOD);
						logger.info("Add Value: " + vdb.getScaledDataValue());
						sampledValueContainer.setSampledValue(value);
						list.add(sampledValueContainer);
					}
				}

				i++;
			}

			if (list.size() > 0) {
				logger.info("Send ChannelsUpdated with " + list.size() + " Values");
				connectionHandle.getListener().channelsUpdated(list);
			}
		}
	}

	private static final SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");

	@Override
	public void newMessage(WMBusMessage message) {
		VariableDataStructure vds = null;
		logger.debug(msgToString(MessageFormat.format("\n\n{0} ------- New Message -------", DF.format(new Date()))));
		logger.debug(msgToString("Message Bytes: ", message.asBlob()));

		try {
			vds = message.getVariableDataResponse();
			vds.decode();
		} catch (DecodingException e) {
			logger.debug(msgToString("Unable to fully decode received message:\n", e.getMessage()));
		}

		logger.debug(msgToString(MessageFormat.format("{0}\n", message)));
		servListeners(vds, message.getSecondaryAddress());
	}

	private String msgToString(Object... msg) {
		StringBuilder sb = new StringBuilder();
		for (Object message : msg) {
			if (message instanceof byte[]) {
				sb.append(printHexBinary((byte[]) message));
			}
			else {
				sb.append(message);
			}
		}
		return sb.toString();
	}

	private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

	private String getShortHexStringFromByteArray(byte[] bytes) {

		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	@Override
	public void stoppedListening(IOException arg0) {
		logger.info("The Reciver lost connection reconnect at 10sec");
		boolean success = false;
		while (true) {

			try {
				Thread.sleep(10000);
				connections.forEach((k, v) -> {
					logger.info("Try to reconneced!");
					logger.info("Reconneced successful");
				});
			} catch (InterruptedException e1) {
				e1.printStackTrace();

			} catch (Exception e) {
				logger.info("Reconneced failed, next try 10sek later");
				e.printStackTrace();
			}
			if (success)
				break;
		}

	}

	@Override
	public void discardedBytes(byte[] arg0) {

	}

	@Override
	public void startChannelScan(DeviceLocator device, ChannelScanListener listener)
			throws UnsupportedOperationException {
		// TODO Auto-generated method stub

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
	public void writeChannel(ChannelLocator channelLocator, Value value)
			throws IOException, NoSuchDeviceException, NoSuchChannelException {
		throw new UnsupportedOperationException();
	}

}
