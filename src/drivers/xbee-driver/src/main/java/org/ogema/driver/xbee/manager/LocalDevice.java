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
package org.ogema.driver.xbee.manager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.DatatypeConverter;

import org.ogema.driver.xbee.Configuration;
import org.ogema.driver.xbee.Connection;
import org.ogema.driver.xbee.Constants;
import org.ogema.driver.xbee.frames.AbstractXBeeMessage;
import org.ogema.driver.xbee.frames.AtCommand;
import org.ogema.driver.xbee.frames.ExplicitAddressingCommandFrame;
import org.ogema.driver.xbee.frames.WrongFormatException;
import org.ogema.driver.xbee.manager.RemoteDevice.DeviceStates;
import org.ogema.driver.xbee.manager.RemoteDevice.InitStates;
import org.ogema.driver.xbee.serialconnection.SerialConnection; // TODO: Enable/Disable joining
// TODO: Touchlink commissioning for ZLL devices
import org.slf4j.Logger;

import jssc.SerialPortException;

public class LocalDevice {
	/**
	 * Contains all remote devices with their 64 bit
	 */
	private final Map<Long, RemoteDevice> devices;
	/**
	 * Maps the 16 bit network addresses (key) to the 64 bit IEEE addresses (value)
	 */
	private final AddressMappings addressMappings;
	private final SerialConnection connection;
	private final Connection ogemaConnection;
	private final InputHandler inputHandler;
	private final DeviceHandler deviceHandler;
	private final Thread deviceHandlerThread;
	private final Map<Byte, Long> frameIdToDestination; // frameID -> destination64BitAddress
	private final MessageHandler messageHandler;
	private final Thread messageHandlerThread;
	private int cyclicSleepPeriod = 0xAF0 * 10; // placeholder value, will be replaced by the result of atCommandReadSp
	private volatile byte frameIdCounter = 0x00;
	private volatile byte sequenceNumberCounter = 0x00;
	private final byte[] atCommandReadSp = { 0x7E, 0x00, 0x04, 0x08, 0x01, 0x53, 0x50, 0x53 };
	private final Logger logger = org.slf4j.LoggerFactory.getLogger("xbee-driver");
	private File remoteDevicesFile;

	public LocalDevice(String serialPort, Connection ogemaConnection) throws SerialPortException {
		devices = new ConcurrentHashMap<Long, RemoteDevice>();
		frameIdToDestination = new ConcurrentHashMap<Byte, Long>();
		addressMappings = new AddressMappings();

		this.ogemaConnection = ogemaConnection;

		deviceHandler = new DeviceHandler(this, cyclicSleepPeriod);
		deviceHandlerThread = new Thread(deviceHandler);
		deviceHandlerThread.setName("xbee-driver-deviceHandler");
		deviceHandlerThread.start();

		messageHandler = new MessageHandler(this, cyclicSleepPeriod);
		messageHandlerThread = new Thread(messageHandler);
		messageHandlerThread.setName("xbee-driver-messageHandler");
		messageHandlerThread.start();

		inputHandler = new InputHandler(this);

		try {
			connection = new SerialConnection(serialPort, inputHandler);
		} catch (SerialPortException e) {
			throw e;
		}

		connection.sendFrame(atCommandReadSp);
		remoteDevicesFile = new File("./config", "zigbee.devices");
		if (remoteDevicesFile.exists())
			readRemoteDevicesFile();

		deviceHandler.initNetworkScan();
	}

	/**
	 * All remote devices are stored in this file. The format is as follows:
	 * ZigBee:64BitIEEEAddress:ND:rawNodeDescriptor:UD:userDescription:EP:endpointID:
	 * rawSimpleDescriptor:EP:endpointID:rawSimpleDescriptor... String:long:char[2]:byte
	 * []:String:char[]:String:byte:byte[]:String:byte:byte[]...
	 */
	private synchronized void readRemoteDevicesFile() {
		synchronized (remoteDevicesFile) {
			BufferedReader br;
			try {
				br = new BufferedReader(new FileReader(remoteDevicesFile));
				String line;
				while ((line = br.readLine()) != null) {
					boolean isXBee = false;
					String[] splitLine = line.split(":"); // TODO userDescriptor must not contain a ':'
					if (splitLine[0].equals("XBee")) {
						isXBee = true;
					}
					else {
						isXBee = false;
					}
					byte[] addressArray = DatatypeConverter.parseHexBinary(splitLine[1]);
					if (8 == addressArray.length) {
						// Read 64bit address, create new RemoteDevice with the
						// address and start init by requesting the 16bit
						// network address
						ByteBuffer bb = ByteBuffer.wrap(addressArray);
						long address64Bit = bb.getLong();
						RemoteDevice remoteDevice = null;
						if (isXBee) {
							remoteDevice = new XBeeDevice(address64Bit, (short) 0); // TODO does XBee respond to
							// Nwk_Addr_Req?
						}
						else {
							remoteDevice = new RemoteDevice(address64Bit, (short) 0);
						}
						remoteDevice.setInitState(InitStates.UNINITIALIZED);
						remoteDevice.setDeviceState(DeviceStates.IDLE);
						devices.put(address64Bit, remoteDevice);
						String status = "";
						byte endpointId = 0;
						boolean endpointIdRead = false;
						for (int i = 2; i < splitLine.length; ++i) {
							switch (splitLine[i]) {
							case "ND":
								status = "ND";
								continue;
							case "UD":
								status = "UD";
								continue;
							case "EP":
								status = "EP";
								continue;
							}
							switch (status) {
							case "ND":
								NodeDescriptor nodeDescriptor = new NodeDescriptor();
								if (!splitLine[i].equals("00")) {
									nodeDescriptor.setRawNodeDescriptor(DatatypeConverter.parseHexBinary(splitLine[i]));
									nodeDescriptor.parseRawNodeDescriptor();
								}
								remoteDevice.setNodeDescriptor(nodeDescriptor);
								break;
							case "UD":
								UserDescriptor userDescriptor = new UserDescriptor();
								userDescriptor.setUserDescription(splitLine[i].toCharArray());
								remoteDevice.setUserDescriptor(userDescriptor);
								break;
							case "EP":
								if (!endpointIdRead) {
									endpointId = DatatypeConverter.parseHexBinary(splitLine[i])[0];
									Endpoint endpoint = new Endpoint(remoteDevice, endpointId);
									remoteDevice.endpoints.put(endpointId, endpoint);
									endpointIdRead = true;
								}
								else { // simpleDescriptor
									SimpleDescriptor simpleDescriptor = new SimpleDescriptor();
									simpleDescriptor.setRawSimpleDescriptor(DatatypeConverter
											.parseHexBinary(splitLine[i]));
									if (!isXBee)
										simpleDescriptor.parseRawSimpleDescriptor();
									Endpoint endpoint = remoteDevice.endpoints.get(endpointId);
									endpoint.setSimpleDescriptor(simpleDescriptor);
									if (!isXBee)
										endpoint.parseClusters();
									endpointIdRead = false;
								}
								break;
							}
						}
					}
					else {
						// Do nothing, this line is probably corrupted
					}
				}
				br.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// Notify deviceHandler so he can start initializing the devices
		synchronized (deviceHandler.deviceHandlerLock) {
			deviceHandler.deviceHandlerLock.notify();
		}
	}

	/**
	 * All remote devices are stored in this file. The format is as follows:
	 * ZigBee:64BitIEEEAddress:ND:rawNodeDescriptor:UD:userDescription:EP:endpointID:
	 * rawSimpleDescriptor:EP:endpointID:rawSimpleDescriptor... String:long:char[2]:byte
	 * []:String:char[]:String:byte:byte[]:String:byte:byte[]...
	 * 
	 * @param readRemoteDevicesFile
	 */
	public synchronized void writeRemoteDevicesFile() {
		synchronized (remoteDevicesFile) {
			// TODO clear file before rewriting it
			// delete and recreate?

			BufferedWriter bw = AccessController.doPrivileged(new PrivilegedAction<BufferedWriter>() {
				public BufferedWriter run() {
					BufferedWriter writer = null;
					try {
						writer = new BufferedWriter(new FileWriter(remoteDevicesFile));
					} catch (IOException e) {
						e.printStackTrace();
					}
					return writer;
				}
			});
			for (Map.Entry<Long, RemoteDevice> deviceEntry : devices.entrySet()) {
				if (!deviceEntry.getValue().getInitState().equals(InitStates.INITIALIZED))
					continue;
				try {
					boolean isXBee = false;
					if (deviceEntry.getValue() instanceof XBeeDevice) {
						bw.write("XBee");
						isXBee = true;
					}
					else {
						bw.write("ZigBee");
						isXBee = false;
					}
					bw.write(":");
					String address64Bit = Long.toHexString(deviceEntry.getKey());
					address64Bit = ("0000000000000000" + address64Bit).substring(address64Bit.length()); // Leading 0s
					bw.write(address64Bit);
					bw.write(":");
					byte[] ba = deviceEntry.getValue().getNodeDescriptor().getRawNodeDescriptor();
					if (ba != null && ba.length != 0) {
						bw.write("ND");
						bw.write(":");
						bw.write(Constants.bytesToHex(ba));
						bw.write(":");
					}
					else {
						bw.write("ND");
						bw.write(":");
						bw.write("00");
						bw.write(":");
					}

					if (deviceEntry.getValue().getNodeDescriptor().hasUserDescriptor()) {
						bw.write("UD");
						bw.write(":");
						bw.write(deviceEntry.getValue().getUserDescriptor().getUserDescription());
						bw.write(":");
					}
					else if (isXBee) {
						bw.write("UD");
						bw.write(":");
						bw.write(deviceEntry.getValue().getNodeIdentifier());
						bw.write(":");
					}
					for (Map.Entry<Byte, Endpoint> endpointEntry : deviceEntry.getValue().endpoints.entrySet()) {
						bw.write("EP");
						bw.write(":");
						bw.write(Constants.bytesToHex(endpointEntry.getKey()));
						bw.write(":");
						if (!isXBee) {
							bw.write(Constants.bytesToHex(endpointEntry.getValue().getSimpleDescriptor()
									.getRawSimpleDescriptor()));
							bw.write(":");
						}
					}
					bw.newLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			try {
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void closeSerialConnection() throws SerialPortException {
		connection.closeConnection();
	}

	public Object getDeviceHandlerLock() {
		return deviceHandler.getDeviceHandlerLock();
	}

	public DeviceHandler getDeviceHandler() {
		return deviceHandler;
	}

	public int getCyclicSleepPeriod() {
		return cyclicSleepPeriod;
	}

	/**
	 * Use this method to send messages for which you require a response.
	 */
	public void sendRequestMessage(byte[] frame, byte responseType, Object lockObject) {
		messageHandler.sendMessage(frame, responseType, lockObject);
	}

	/**
	 * Sends the message as it is by building a complete frame using the FrameFactory.
	 * 
	 * @param message
	 */
	public void sendMessage(AbstractXBeeMessage message) {
		try {
			connection.sendFrame(XBeeFrameFactory.composeMessageToFrame(message.getMessage()));
		} catch (WrongFormatException e) {
			e.printStackTrace();
		}
	}

	/**
	 * The XBee Cyclic Sleep Period (SP) has a range of 0x20 - 0xAFO * 10ms. The value * 10ms has to be added manually.
	 * P determines the transmission timeout when sending to a sleeping end device. SP also determines how long the
	 * parent will buffer a message for a sleeping child.
	 * 
	 * @param cyclicSleepPeriod
	 */
	public void setCyclicSleepPeriod(short cyclicSleepPeriod) {
		this.cyclicSleepPeriod = cyclicSleepPeriod * 10;
		deviceHandler.setCyclicSleepPeriod(this.cyclicSleepPeriod);
		messageHandler.setCyclicSleepPeriod(this.cyclicSleepPeriod);
		logger.debug("\n\n****CyclicSlepp: " + Integer.toHexString(cyclicSleepPeriod & 0xffff) + "\n\n\n");
	}

	/**
	 * Takes a Message, replaces frameId and sequenceNumber with generated values, builds the frame and sends it.
	 * 
	 * @param message
	 */
	public void sendMessage(byte[] message) {
		if (Configuration.DEBUG)
			logger.debug("Start sending message: " + Constants.bytesToHex(message));
		if (message[16] == 0x00 && message[17] == 0x00) { // ZDP Profile, this means no
			// commandId
			message[20] = getSequenceNumber(); // Overwrite the placeholder
			// sequence number
		}
		else if (message[20] == 0x04) { // manufacturer specific
			message[23] = getSequenceNumber(); // Overwrite the placeholder
			// sequence number
		}
		else {
			message[21] = getSequenceNumber(); // Overwrite the placeholder
			// sequence number
		}
		try {
			connection.sendFrame(XBeeFrameFactory.composeMessageToFrame(message));
		} catch (WrongFormatException e) {
			e.printStackTrace();
		}
		try {
			if (Configuration.DEBUG)
				logger.debug(" ### Sent frame: "
						+ Constants.bytesToHex(XBeeFrameFactory.composeMessageToFrame(message)));
		} catch (WrongFormatException e) {
			e.printStackTrace();
		}
	}

	public MessageHandler getMessageHandler() {
		return messageHandler;
	}

	public byte getFrameId() {
		if (frameIdCounter == 0x00) // Never use 0
			++frameIdCounter;
		return frameIdCounter++;
	}

	public byte getSequenceNumber() {
		return sequenceNumberCounter++;
	}

	/**
	 * Send a complete frame that already contains start delimiter, length and checksum
	 * 
	 * @param frame
	 */
	public void sendFrame(byte[] frame) {
		if (Configuration.DEBUG)
			logger.debug("Sending frame: " + Constants.bytesToHex(frame));
		connection.sendFrame(frame);
	}

	public SerialConnection getConnection() {
		return connection;
	}

	public void close() {
		try {
			closeSerialConnection();
		} catch (SerialPortException e) {
			e.printStackTrace();
		}
		deviceHandler.stop();
		messageHandler.stop();
	}

	public void setNodeJoinTime(byte njArray) {
		AtCommand njCommand = new AtCommand(getFrameId(), (short) 0x4E4A, njArray);
		sendMessage(njCommand);
	}

	public boolean isKnownDevice(long sourceAddress64Bit) {
		return devices.containsKey(sourceAddress64Bit);
	}

	public boolean isKnownDevice(short sourceAddress16Bit) {
		if (addressMappings.containsAddress(sourceAddress16Bit)) {
			return devices.containsKey(addressMappings.getAddress64Bit(sourceAddress16Bit));
		}
		else {
			return false;
		}
	}

	public void addRemoteDevice(long sourceAddress64Bit, short sourceAddress16Bit) {
		devices.put(sourceAddress64Bit, new RemoteDevice(sourceAddress64Bit, sourceAddress16Bit));
		addressMappings.addAddressMapping(sourceAddress64Bit, sourceAddress16Bit);
	}

	public void addRemoteXBeeDevice(long sourceAddress64Bit, short sourceAddress16Bit) {
		devices.put(sourceAddress64Bit, new XBeeDevice(sourceAddress64Bit, sourceAddress16Bit));
		addressMappings.addAddressMapping(sourceAddress64Bit, sourceAddress16Bit);
	}

	public RemoteDevice getRemoteDevice(long sourceAddress64Bit) {
		return devices.get(sourceAddress64Bit);
	}

	public RemoteDevice getRemoteDevice(short sourceAddress16Bit) {
		return devices.get(addressMappings.getAddress64Bit(sourceAddress16Bit));
	}

	public long get64BitAddress(short address16Bit) {
		return addressMappings.getAddress64Bit(address16Bit);
	}

	public void removeRemoteDevice(short address16Bit) {
		ogemaConnection.removeDevices(addressMappings.getAddress64Bit(address16Bit));
		devices.remove(addressMappings.getAddress64Bit(address16Bit));
		addressMappings.removeAddressMapping(address16Bit);
	}

	public void removeRemoteDevice(long address64Bit) {
		ogemaConnection.removeDevices(address64Bit);
		devices.remove(address64Bit);
		addressMappings.removeAddressMapping(address64Bit);
	}

	public Map<Long, RemoteDevice> getDevices() {
		return devices;
	}

	public long getDestinationFromFrameId(byte frameId) {
		if (frameIdToDestination.get(frameId) != null) {
			return frameIdToDestination.get(frameId);
		}
		else {
			return 0;
		}
	}

	public void setFrameIdToDestination(byte frameId, long destination) {
		frameIdToDestination.put(frameId, destination);
	}

	public void readAttributes(Endpoint remoteEndpoint, byte localEndpoint, short clusterId, ByteBuffer payloadBuffer) {
		// TODO Make this method more flexible e. g. by using the endpoint class
		if (localEndpoint == 0x01 && clusterId == 0x000A) {
			// TODO now assuming only 1 command with certain ID
			if ((Short.reverseBytes(payloadBuffer.getShort()) & 0xffff) == 0x0000) { // UTCTime read request
				int secondsSinceEpoch = (int) (java.lang.System.currentTimeMillis() / 1000) - 946684800; // 946684800
				// seconds
				// between
				// 1.1.1970
				// and
				// 1.1.2000
				if (Configuration.DEBUG) {
					logger.debug("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n*****UTC Time: " + secondsSinceEpoch);
					logger.debug("*****UTC Time Reversed: " + Integer.reverseBytes(secondsSinceEpoch));
				}
				ByteBuffer responseBuffer = ByteBuffer.allocate(10);
				responseBuffer.put((byte) 0x00); // ZCL HEADER
				responseBuffer.put((byte) 0x00); // ZCL HEADER Sequence number
				responseBuffer.putShort(Short.reverseBytes(clusterId)); // CLUSTER ID
				responseBuffer.put(Constants.STATUS_SUCCESS);
				responseBuffer.put(Constants.UTCTIME);
				responseBuffer.putInt(Integer.reverseBytes(secondsSinceEpoch));
				sendReadAttributesResponse(remoteEndpoint, localEndpoint, clusterId, responseBuffer);
			}
		}
	}

	private void sendReadAttributesResponse(Endpoint remoteEndpoint, byte localEndpoint, short clusterId,
			ByteBuffer responseBuffer) {
		// TODO remoteEndpoint.getProfileId() is wrong, it's 0000 but it should be 0104
		ExplicitAddressingCommandFrame responseFrame = new ExplicitAddressingCommandFrame(getFrameId(), remoteEndpoint
				.getDevice().getAddress64Bit(), remoteEndpoint.getDevice().getAddress16Bit(), localEndpoint,
				remoteEndpoint.getEndpointId(), clusterId, (short) 0x0104, (byte) 0x00, (byte) 0x00, responseBuffer
						.array());
		sendMessage(responseFrame.getMessage());
	}

	public AddressMappings getAddressMappings() {
		return addressMappings;
	}
}
