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
package org.ogema.driver.wmbus;

import java.io.IOException;
import java.net.ConnectException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
import org.ogema.core.channelmanager.measurements.DoubleValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.openmuc.jmbus.DecodingException;
import org.openmuc.jmbus.HexConverter;
import org.openmuc.jmbus.VariableDataBlock;
import org.openmuc.jmbus.VariableDataResponse;
import org.openmuc.jmbus.wireless.MBusRfMode;
import org.openmuc.jmbus.wireless.WMBusListener;
import org.openmuc.jmbus.wireless.WMBusMessage;
import org.openmuc.jmbus.wireless.WMBusSapAmber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = false)
@Service(ChannelDriver.class)
public class WirelessMBusDriver implements ChannelDriver, WMBusListener {
	private final static Logger logger = LoggerFactory.getLogger(WirelessMBusDriver.class);

	private final Map<String, ConnectionHandle> connections = new HashMap<String, ConnectionHandle>();
	private final List<ConnectionHandle> tranciverList = new LinkedList<ConnectionHandle>();
	private final Map<String, ChannelUpdateListener> listeners = new HashMap<String, ChannelUpdateListener>();
	private List<SampledValueContainer> sampledValueContainers = new LinkedList<SampledValueContainer>();
	private final static String ID = "wmbus";
	private final static String DESCRIPTION = "This is a driver to communicate with wireless M-Bus Devices";
	private Object mSAP;

	public enum TRANCECIVER {

		AMBER, RADIOCRAFT

	}

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
	public List<ChannelLocator> getChannelList(DeviceLocator device) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void readChannels(List<SampledValueContainer> channels) throws UnsupportedOperationException, IOException {
		throw new UnsupportedOperationException("Synchone Comunikation (Mode T) is not supportet yet");
	}

	@Override
	public void readChannels(List<SampledValueContainer> channels, ChannelUpdateListener listener)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
		// TODO Auto-generated method stub
	}

	@Override
	public void listenChannels(List<SampledValueContainer> channels, ChannelUpdateListener listener)
			throws UnsupportedOperationException, NoSuchDeviceException, NoSuchChannelException, IOException {

		sampledValueContainers = channels;

		for (SampledValueContainer connection : channels) {
			String interfaceName = connection.getChannelLocator().getDeviceLocator().getInterfaceName();
			String tranceciverSting = substringAfter(interfaceName, ":");
			if (tranceciverSting.equalsIgnoreCase("AMBER")) {
				ConnectionHandle handle = connections.get(connection.getChannelLocator().getDeviceLocator()
						.getInterfaceName());
				if (handle == null) {
					throw new NoSuchDeviceException(connection.getChannelLocator().getDeviceLocator());
				}

				final WMBusSapAmber mBusSap = (WMBusSapAmber) handle.getMBusSap();

				handle.setListener(listener);
				mBusSap.setKey(connection.getChannelLocator().getDeviceLocator().getDeviceAddress(),
						hexStringToByteArray(connection.getChannelLocator().getDeviceLocator().getParameters()));
				if (!handle.isOpen()) {
					try {

						String string = connection.getChannelLocator().getDeviceLocator().getInterfaceName();
						String modeAsSting = substringAfter(string, "!");
						modeAsSting = substringBefore(modeAsSting, ":");

						mBusSap.open();
						handle.open();
					} catch (IOException e2) {
						System.err.println("Failed to open serial port: " + e2.getMessage());

						class ReconnectThread extends Thread {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								while (true) {
									logger.info("Reconneced at 30sec");
									try {
										Thread.sleep(30000);
									} catch (InterruptedException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
									try {
										mBusSap.open();
										logger.info("reconnect successful");
										break;
									} catch (IOException e) {
										// TODO Auto-generated catch block
										System.err.println("Failed to open serial port: " + e.getMessage());
									}
								}

							}

						}

						ReconnectThread reconnectThread = new ReconnectThread();
						reconnectThread.start();

					}
				}
			}
		}
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
	public void writeChannels(List<ValueContainer> channels) throws UnsupportedOperationException, IOException,
			NoSuchDeviceException, NoSuchChannelException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public void writeChannels(List<ValueContainer> channels, ExceptionListener listener)
			throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

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

		String string = channel.getDeviceLocator().getInterfaceName();
		String tranceciverSting = substringAfter(string, ":");
		String modeAsSting = substringAfter(string, "!");
		modeAsSting = substringBefore(modeAsSting, ":");
		MBusRfMode mode = MBusRfMode.S;
		TRANCECIVER tranceciver = TRANCECIVER.AMBER;

		switch (tranceciverSting) {
		case "AMBER":
			tranceciver = TRANCECIVER.AMBER;
			break;
		case "RADIOCRAFT":
			tranceciver = TRANCECIVER.RADIOCRAFT;
			break;

		default:
			break;
		}

		switch (modeAsSting) {
		case "S":
			mode = MBusRfMode.S;
			break;

		case "T1":
			mode = MBusRfMode.T1;
			break;
		case "T2":
			mode = MBusRfMode.T2;
			break;

		default:
			logger.info("THE MODE ISN'T CORRECT, (S,T1,T2 ARE POSSIBLE). YOUR MODE: " + modeAsSting);
			logger.info("Used mode is now:S");
			mode = MBusRfMode.S;
			break;
		}

		ConnectionHandle connectionHandle = connections.get(channel.getDeviceLocator().getInterfaceName());
		if (connectionHandle == null) {
			if (tranceciver == TRANCECIVER.AMBER) {
				WMBusSapAmber amberSap = new WMBusSapAmber(substringBefore(string, "!"), mode, this);
				mSAP = amberSap;
				connectionHandle = new ConnectionHandle(amberSap, channel, tranceciver);
				try {
					amberSap.open();
					connectionHandle.open();
					amberSap.close();
					connectionHandle.close();
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

			} // RADIO CRAFT!
			else if (tranceciver == TRANCECIVER.RADIOCRAFT) {
				logger.error("NOT IMPLEMENTED YET");
			}
			connections.put(channel.getDeviceLocator().getInterfaceName(), connectionHandle);
			tranciverList.add(connectionHandle);
		}
		else {
			connectionHandle.getChannels().add(channel);

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
			switch (connectionHandle.tranceciver) {
			case AMBER:
				WMBusSapAmber amberSap = (WMBusSapAmber) connectionHandle.getMBusSap();
				amberSap.close();
				connectionHandle.getChannels().remove(channel);
				if (connectionHandle.getChannels().size() <= 0) {
					connectionHandle.close();
				}
				break;
			case RADIOCRAFT:
				logger.error("NOT IMPLEMENTED YET");
				break;
			default:
				break;
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

	@Override
	public void newMessage(WMBusMessage message) {

		long timestamp = System.currentTimeMillis();
		try {

			message.decodeDeep();
			VariableDataResponse vdr = message.getVariableDataResponse();

			List<VariableDataBlock> vdbs = vdr.getVariableDataBlocks();
			String[] dibvibs = new String[vdbs.size()];

			int i = 0;
			for (VariableDataBlock vdb : vdbs) {
				dibvibs[i++] = bytesToHexString(vdb.getDIB()) + ':' + bytesToHexString(vdb.getVIB());
			}
			i = 0;
			logger.info("Message received, Time:" + System.currentTimeMillis() + "\t DeviceAddress: "
					+ HexConverter.getShortHexStringFromByteArray(message.getMBusId().asByteArray()));
			List<SampledValueContainer> list = new LinkedList<SampledValueContainer>();
			for (ConnectionHandle connectionHandle : tranciverList) {

				for (VariableDataBlock vdb : vdr.getVariableDataBlocks()) {

					for (SampledValueContainer sampledValueContainer : sampledValueContainers) {
						String channelAddress = sampledValueContainer.getChannelLocator().getChannelAddress();
						String deviceAddress = sampledValueContainer.getChannelLocator().getDeviceLocator()
								.getDeviceAddress();
						String deviceAddressFromMessage = HexConverter.getShortHexStringFromByteArray(message
								.getMBusId().asByteArray());

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

		} catch (DecodingException e) {
			logger.info("Unable to fully decode received message: " + e.getMessage());
		}

	}

	@Override
	public void stoppedListening(IOException arg0) {
		logger.info("The Reciver lost connection reconnect at 10sec");

		while (true) {

			try {
				Thread.sleep(10000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			WMBusSapAmber amberSAP = (WMBusSapAmber) mSAP;
			try {
				logger.info("Try to reconneced!");
				amberSAP.open();
				logger.info("Reconneced successful");
				break;

			} catch (IOException e) {
				logger.info("Reconneced failed, next try 10sek later");
				// TODO Auto-generated catch block

			} catch (Exception e) {
				e.printStackTrace();
			}
			// TODO Auto-generated method stub

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

}
