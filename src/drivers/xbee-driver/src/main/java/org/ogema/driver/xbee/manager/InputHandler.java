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

import java.nio.ByteBuffer;

import org.ogema.core.channelmanager.measurements.ByteArrayValue;
import org.ogema.driver.xbee.Configuration;
import org.ogema.driver.xbee.Constants;
import org.ogema.driver.xbee.frames.RemoteAtCommand;
import org.ogema.driver.xbee.frames.WrongFormatException;
import org.ogema.driver.xbee.manager.zdo.MatchDescriptorResponse;
import org.ogema.driver.xbee.manager.zdo.MgmtLqiRequest;
import org.ogema.driver.xbee.manager.zdo.SimpleDescriptorRequest;
import org.slf4j.Logger;

// TODO: Use frameIds to and check for successful transmission
// TODO: Check if devices already are in map and if update necessary
/**
 * Handles incoming messages and responds accordingly.
 * 
 * @author puschas
 * 
 */
public class InputHandler implements Runnable {
	public enum ResponseType {
		ACTIVE_ENDPOINT_RESPONSE, SIMPLE_DESCRIPTOR_RESPONSE, NODE_DESCRIPTOR_RESPONSE, IEEE_ADDR_RESPONSE, READ_ATTRIBUTES_RESPONSE, TRANSMIT_STATUS, WRITE_ATTRIBUTES_RESPONSE, REMOTE_NI_COMMAND, COMPLEX_DESCRIPTOR_RESPONSE, USER_DESCRIPTOR_RESPONSE, NETWORK_ADDRESS_RESPONSE
	};

	private volatile boolean running;
	private final Object inputEventLock;
	private final Object deviceHandlerLock;
	private DeviceHandler deviceHandler;
	private MessageHandler messageHandler;
	private ByteBuffer byteBuffer;
	private byte[] tempArray;
	private SimpleDescriptorRequest simpleDescriptorRequest = new SimpleDescriptorRequest();
	private long address64Bit;
	private short address16Bit;
	private long sourceAddress64Bit;
	private short sourceAddress16Bit;
	private byte sourceEndpoint;
	private byte destinationEndpoint;
	private byte frameId;
	private short clusterId;
	private short profileId;
	private LocalDevice localDevice;
	private short nwkAddrOfInterest;
	private final Logger logger = org.slf4j.LoggerFactory.getLogger("xbee-driver");

	public InputHandler(LocalDevice localDevice) {
		inputEventLock = localDevice.getInputEventLock();
		deviceHandlerLock = localDevice.getDeviceHandlerLock();
		deviceHandler = localDevice.getDeviceHandler();
		messageHandler = localDevice.getMessageHandler();
		running = true;
		this.localDevice = localDevice;
	}

	/**
	 * 
	 * @param running
	 *            Stops the loop in run().
	 */
	public void stop() {
		this.running = false;
	}

	@Override
	public void run() {
		while (running) {
			synchronized (inputEventLock) {
				while (!localDevice.connectionHasFrames()) {
					try {
						inputEventLock.wait();
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				tempArray = localDevice.getReceivedFrame();
			}
			if (Configuration.DEBUG)
				logger.debug("New input in handler: " + Constants.bytesToHex(tempArray));
			handleMessage();
		}
	}

	private void handleMessage() {
		byteBuffer = ByteBuffer.wrap(tempArray);
		switch (byteBuffer.get()) { // Check frameType
		case Constants.EXPLICIT_RX_INDICATOR:
			handleExplicitRxIndicator();
			break;
		case Constants.AT_COMMAND_RESPONSE:
			handleAtCommandResponse();
			break;
		case Constants.REMOTE_COMMAND_RESPONSE:
			handleRemoteCommandResponse();
			break;
		case Constants.TX_STATUS:
			messageHandler.txStatusReceived(byteBuffer);
			break;
		default:
			if (Configuration.DEBUG)
				logger.error("unknown frameType");
			break;
		}
	}

	private void handleRemoteCommandResponse() {
		frameId = byteBuffer.get();
		if (Configuration.DEBUG)
			logger.debug("Received Frame ID: " + frameId);
		sourceAddress64Bit = byteBuffer.getLong();
		sourceAddress16Bit = byteBuffer.getShort();
		switch (byteBuffer.getShort()) { // AT Cmd
		case Constants.NI_COMMAND:
			handleRemoteNiCommand();
			break;
		}
	}

	private void handleRemoteNiCommand() {
		if (Constants.STATUS_SUCCESS != byteBuffer.get())
			return; // No success
		if (!isDeviceAddress(sourceAddress64Bit) || !isDeviceAddress(sourceAddress16Bit)) {
			return;
		}
		XBeeDevice xBeeDevice = (XBeeDevice) localDevice.getRemoteDevice(sourceAddress64Bit);
		byte temp[] = new byte[byteBuffer.limit() - byteBuffer.position()];
		byteBuffer.get(temp, 0, byteBuffer.limit() - byteBuffer.position());
		byteBuffer = ByteBuffer.wrap(temp);

		xBeeDevice.parseNodeIdentifier(byteBuffer);

		deviceHandler.receivedResponse(sourceAddress16Bit, ResponseType.REMOTE_NI_COMMAND);
	}

	private void handleAtCommandResponse() {
		frameId = byteBuffer.get();
		if (Configuration.DEBUG)
			logger.debug("Received Frame ID: " + frameId);
		switch (byteBuffer.getShort()) { // AT Cmd
		case Constants.SP_COMMAND:
			handleSpCommand();
			break;
		}
	}

	private void handleSpCommand() {
		if (Constants.STATUS_SUCCESS != byteBuffer.get())
			return; // No success
		localDevice.setCyclicSleepPeriod(byteBuffer.getShort());
	}

	private void handleExplicitRxIndicator() {
		if (Configuration.DEBUG)
			logger.debug("Handle RX Indicator");
		sourceAddress64Bit = byteBuffer.getLong();
		sourceAddress16Bit = byteBuffer.getShort();
		sourceEndpoint = byteBuffer.get();
		destinationEndpoint = byteBuffer.get();
		clusterId = byteBuffer.getShort();
		profileId = (short) (byteBuffer.getShort() & 0xffff);
		byteBuffer.get(); // Omit receive options
		if (0 == profileId) { // ZDO Commands
			switch (clusterId) {
			case Constants.DEVICE_ANNCE:
				handleDeviceAnnce();
				break;
			case Constants.NETWORK_ADDRESS_RESPONSE:
				handleNetworkAddressResponse();
				break;
			case Constants.ACTIVE_ENDPOINT_RESPONSE:
				handleActiveEndpointResponse();
				break;
			case Constants.SIMPLE_DESCRIPTOR_RESPONSE:
				handleSimpleDescriptorResponse();
				break;
			case Constants.NODE_DESCRIPTOR_RESPONSE:
				handleNodeDescriptorResponse();
				break;
			case Constants.COMPLEX_DESCRIPTOR_RESPONSE:
				handleComplexDescriptorResponse();
				break;
			case Constants.USER_DESCRIPTOR_RESPONSE:
				handleUserDescriptorResponse();
				break;
			case Constants.MATCH_DESCRIPTOR_REQUEST:
				handleMatchDescriptorRequest();
				break;
			case Constants.MGMT_LQI_RESPONSE:
				handleMgmtLqiResponse();
				break;
			}
		}
		else if ("c105".equals(Integer.toHexString(profileId & 0xffff))) { // Digi
			if (Configuration.DEBUG)
				logger.debug("############################ Message from XBee device received!!!");
			if (!localDevice.isKnownDevice(sourceAddress64Bit)) {
				if (isDeviceAddress(sourceAddress64Bit) && isDeviceAddress(sourceAddress16Bit)) {
					localDevice.addRemoteXBeeDevice(sourceAddress64Bit, sourceAddress16Bit);
				}
				else {
					return;
				}
				RemoteAtCommand niRequest = new RemoteAtCommand(localDevice.getFrameId(), sourceAddress64Bit,
						sourceAddress16Bit, (byte) 0x00, (short) 0x4E49, null);
				localDevice.sendMessage(niRequest);
				return;
			}
			// Now check if this device has falsely been added as a regular
			// RemoteDevice instead of an XBeeDevice

			RemoteDevice remoteDevice = localDevice.getRemoteDevice(sourceAddress64Bit);

			if (!(remoteDevice instanceof XBeeDevice)) {
				// Delete RemoteDevice and recreate as XBeeDevice
				long address64Bit = remoteDevice.getAddress64Bit();
				short address16Bit = remoteDevice.getAddress16Bit();
				localDevice.removeRemoteDevice(address16Bit);
				localDevice.addRemoteXBeeDevice(address64Bit, address16Bit);
				synchronized (deviceHandlerLock) {
					deviceHandlerLock.notify();
				}
				return;
			}

			XBeeDevice xbeeDevice = (XBeeDevice) remoteDevice;

			if (xbeeDevice.getNodeIdentifier() == null) {
				logger.debug("Waiting for NI");
			}
			else if (0x0011 == clusterId) {
				switch (xbeeDevice.getNodeIdentifier()) { // Check if supported device. TODO maybe omit the check and
				// use this for all Digi devices?
				case ("ZBS-110V2"):
				case ("ZBS-122"):
				case ("HA Sensorknoten"):
					byte[] tempArray = new byte[byteBuffer.limit() - byteBuffer.position()];
					byteBuffer.get(tempArray, 0, byteBuffer.limit() - byteBuffer.position());
					xbeeDevice.setValue(new ByteArrayValue(tempArray));
					break;
				}
			}
		}
		else if (0x0104 == profileId) { // Home Automation
			if (!localDevice.isKnownDevice(sourceAddress64Bit)) { // Add
				// device
				// if
				// unknown
				if (isDeviceAddress(sourceAddress64Bit) && isDeviceAddress(sourceAddress16Bit)) {
					localDevice.addRemoteDevice(sourceAddress64Bit, sourceAddress16Bit);
					logger.debug("added device because of home automation message");
				}
				else {
					return;
				}
			}
			byte[] payload = new byte[byteBuffer.limit() - byteBuffer.position()];
			byteBuffer.get(payload, 0, byteBuffer.limit() - byteBuffer.position());
			messageHandler.messageReceived(localDevice.getRemoteDevice(sourceAddress64Bit).getEndpoints().get(
					sourceEndpoint), destinationEndpoint, clusterId, payload);
		}
	}

	private void handleNetworkAddressResponse() {
		byteBuffer.get(); // Omit frame control byte
		if (Constants.STATUS_SUCCESS != byteBuffer.get())
			return; // No success
		long address64Bit = Long.reverseBytes(byteBuffer.getLong());
		short address16Bit = Short.reverseBytes(byteBuffer.getShort());
		RemoteDevice remoteDevice = localDevice.getDevices().get(address64Bit);
		remoteDevice.setAddress16Bit(address16Bit);
		localDevice.getAddressMappings().addAddressMapping(address64Bit, address16Bit);
		deviceHandler.receivedResponse(address64Bit, ResponseType.NETWORK_ADDRESS_RESPONSE);
		synchronized (deviceHandlerLock) {
			deviceHandlerLock.notify();
		}
	}

	private void handleMgmtLqiResponse() {
		byteBuffer.get(); // Omit frame control byte
		if (Constants.STATUS_SUCCESS != byteBuffer.get())
			return; // No success
		byte neighborTableEntries = byteBuffer.get(); // Total number of
		// Neighbor Table
		// entries within the
		// Remote Device.
		byte startIndex = byteBuffer.get(); // Starting index within the
		// Neighbor Table to begin reporting
		// for the NeighborTableList.
		byte neighborTableListCount = byteBuffer.get(); // Number of Neighbor
		// Table entries
		// included within
		// NeighborTableList.
		logger.debug("\n\n\n\n\n\n\n\n\n\n\n\nstartIndex: " + startIndex);
		logger.debug("count: " + neighborTableListCount);
		logger.debug("entries: " + neighborTableEntries);
		for (int i = 0; i < (neighborTableListCount & 0xff); ++i) {
			logger.debug("\n\n\n\n###neighbou table entry:");
			long extendedPanId = Long.reverseBytes(byteBuffer.getLong());
			logger.debug("### ExtendedPanId: " + Long.toHexString(extendedPanId));
			long address64Bit = Long.reverseBytes(byteBuffer.getLong());
			logger.debug("### 64bit Address: " + Long.toHexString(address64Bit));
			short address16Bit = Short.reverseBytes(byteBuffer.getShort());
			logger.debug("### 16bit Address: " + Integer.toHexString(address16Bit & 0xffff) + "\n\n");
			byte bitFlags = byteBuffer.get(); // 2 bits device type, 2 bits
			// rxonwhenidle, 3 bits
			// relationship, 1 bit reserved
			byteBuffer.get(); /* byte bitFlags2 = */// 2 bits permit joining, 6 bits
			// reserved
			byteBuffer.get(); /* byte depth = */// 0x00 means coordinator
			byteBuffer.get(); /* byte lqi = */// estimated link quality

			// TODO check if pan ID fits the network? Necessary?
			if (!localDevice.isKnownDevice(address64Bit) && isDeviceAddress(address64Bit)
					&& isDeviceAddress(address16Bit)) {
				localDevice.addRemoteDevice(address64Bit, address16Bit);
				// Only request neighbour table if the device was not known previously, this should prevent loops
				if ((bitFlags & 0B00000011) == 0x01) { // If device is router
					MgmtLqiRequest mgmtLqiRequest = new MgmtLqiRequest();
					localDevice.sendMessage(mgmtLqiRequest.getUnicastMessage(address64Bit, address16Bit));
				}
			}
		}

		synchronized (deviceHandlerLock) {
			deviceHandlerLock.notify();
		}

		// send another request for the remaining entries
		if (neighborTableEntries > (neighborTableListCount & 0xff) + (startIndex & 0xff)) {
			MgmtLqiRequest mgmtLqiRequest = new MgmtLqiRequest(
					(byte) ((neighborTableListCount & 0xff) + (startIndex & 0xff)));
			// TODO test if this works when the source is the coordinator
			localDevice.sendMessage(mgmtLqiRequest.getUnicastMessage(sourceAddress64Bit, sourceAddress16Bit));
		}
	}

	private void handleMatchDescriptorRequest() {
		byteBuffer.get(); // Omit sequence number
		nwkAddrOfInterest = Short.reverseBytes(byteBuffer.getShort()); // Network
		// address
		// of
		// interest
		if (0x0000 != nwkAddrOfInterest) { // Request not for this coordinator
			return;
			// If the NWKAddrOfInterest field does not match the network address
			// of the remote device and it is the coordinator or a router, it
			// shall determine
			// whether the NWKAddrOfInterest field matches the network address
			// of one of its children. If the NWKAddrOfInterest field does not
			// match the network address of
			// one of the children of the remote device, it shall set the Status
			// field to DEVICE_NOT_FOUND, set the MatchLength field to 0, and
			// not include the
			// MatchList field.
			// If the NWKAddrOfInterest matches the network address of one of
			// the children of
			// the remote device, it shall determine whether any simple
			// descriptors for that
			// device are available. If no simple descriptors are available for
			// the child indicated
			// by the NWKAddrOfInterest field, the remote device shall set the
			// Status field to
			// NO_DESCRIPTOR, set the MatchLength field to 0, and not include
			// the
			// MatchList field. If any simple descriptors are available for the
			// child indicated by
			// the NWKAddrOfInterest field, the remote device shall apply the
			// match criterion,
			// as described below, that was specified in the original
			// Match_Desc_req command
			// to each of these simple descriptors.
			// @see ZigBee Specification 192
		}

		profileId = Short.reverseBytes(byteBuffer.getShort());
		int inputClusterCount = byteBuffer.get() & 0xff;
		short[] inputClusters = new short[inputClusterCount];
		for (int i = 0; i < inputClusterCount; ++i) {
			inputClusters[i] = Short.reverseBytes(byteBuffer.getShort());
		}
		int outputClusterCount = byteBuffer.get() & 0xff;
		short[] outputClusters = new short[outputClusterCount];
		for (int i = 0; i < outputClusterCount; ++i) {
			outputClusters[i] = Short.reverseBytes(byteBuffer.getShort());
		}

		logger.debug("\n\n\n\n Output cluster Count: " + outputClusters.length + "\n Input cluster Count: "
				+ inputClusters.length + "\nInput cluster [0]: " + Integer.toHexString(inputClusters[0] & 0xffff)
				+ "\n\n\n");

		if (outputClusters.length == 0 && inputClusters.length == 1) { // TODO make this more flexible and extendable by
			// using endpoint classes for the localDevice
			if (0x000A == inputClusters[0]) {

				ByteBuffer payload = ByteBuffer.allocate(5);
				payload.put(Constants.STATUS_SUCCESS);
				payload.putShort(Short.reverseBytes(nwkAddrOfInterest));
				payload.put((byte) 0x01); // 1 match
				payload.put((byte) 0x01); // Endpoint 0x01 arbitrarily selected

				MatchDescriptorResponse matchDescriptorResponse = new MatchDescriptorResponse(sourceAddress64Bit,
						sourceAddress16Bit, payload.array());
				localDevice.sendMessage(matchDescriptorResponse.getUnicastMessage());

				return;
			}
		}

		// TODO appropriate response
		// for now, tell the requesting device that there is no matching
		// endpoint

		ByteBuffer payload = ByteBuffer.allocate(4);
		payload.put(Constants.NO_DESCRIPTOR);
		payload.putShort(Short.reverseBytes(nwkAddrOfInterest));
		payload.put((byte) 0x00);

		MatchDescriptorResponse matchDescriptorResponse = new MatchDescriptorResponse(sourceAddress64Bit,
				sourceAddress16Bit, payload.array());
		localDevice.sendMessage(matchDescriptorResponse.getUnicastMessage());

		// If the NWKAddrOfInterest matches the network address of one of the
		// children of
		// the remote device, it shall determine whether any simple descriptors
		// for that
		// device are available. If no simple descriptors are available for the
		// child indicated
		// by the NWKAddrOfInterest field, the remote device shall set the
		// Status field to
		// NO_DESCRIPTOR, set the MatchLength field to 0, and not include the
		// MatchList field.

	}

	private void handleUserDescriptorResponse() {
		logger.debug("\n\n\nUser Descriptor response!");
		byteBuffer.get(); // Omit frame control byte
		if (Constants.STATUS_SUCCESS != byteBuffer.get())
			return; // No success
		nwkAddrOfInterest = Short.reverseBytes(byteBuffer.getShort()); // Network
		// address
		// of
		// interest
		UserDescriptor userDescriptor = new UserDescriptor();
		byte length = byteBuffer.get(); // Length in bytes of the
		// UserDescriptor field
		char[] userDescription = new char[length & 0xff];
		for (int i = 0; i < (length & 0xff); ++i) {
			userDescription[i] = (char) byteBuffer.get();
		}
		userDescriptor.setUserDescription(userDescription);
		logger.debug("\n\n\n\nNew user description: " + String.valueOf(userDescription));
		if (localDevice.isKnownDevice(nwkAddrOfInterest)) {
			localDevice.getRemoteDevice(nwkAddrOfInterest).setUserDescriptor(userDescriptor);
			deviceHandler.receivedResponse(nwkAddrOfInterest, ResponseType.USER_DESCRIPTOR_RESPONSE);
			synchronized (deviceHandlerLock) {
				deviceHandlerLock.notify();
			}
		}
	}

	private void handleComplexDescriptorResponse() {
		logger.debug("Complex Descriptor response!");
		byteBuffer.get(); // Omit frame control byte
		if (Constants.STATUS_SUCCESS != byteBuffer.get())
			return; // No success
		nwkAddrOfInterest = Short.reverseBytes(byteBuffer.getShort()); // Network
		// address
		// of
		// interest
		/* TODO ComplexDescriptor complexDescriptor = */new ComplexDescriptor();
		byteBuffer.get(); /* byte length = */// Length in bytes of the
		// ComplexDescriptor field
		// TODO parse complexDescriptor
		// no device for testing available at this time

		// Excerpt from ZigBee Specification:
		// Due to the extended and complex nature of the data in this
		// descriptor, it is
		// presented in XML form using compressed XML tags. Each field of the
		// descriptor,
		// shown in Table 2.40, can therefore be transmitted in any order. As
		// this descriptor
		// needs to be transmitted over air, the overall length of the complex
		// descriptor shall
		// be less than or equal to apscMaxDescriptorSize.
		// @see ZigBee Specification 2.3.2.6 at page 118

	}

	private void handleNodeDescriptorResponse() {
		if (Configuration.DEBUG)
			logger.debug("Node Descriptor response!");
		byteBuffer.get(); // Omit frame control byte
		byte status = byteBuffer.get();

		if (Constants.STATUS_NOT_SUPPORTED == status) {
			NodeDescriptor nodeDescriptor = new NodeDescriptor();
			nwkAddrOfInterest = sourceAddress16Bit;
			if (nwkAddrOfInterest == 0x0000) {
				return;
			}
			else if (localDevice.isKnownDevice(nwkAddrOfInterest)) {
				localDevice.getRemoteDevice(nwkAddrOfInterest).setNodeDescriptor(nodeDescriptor);
				deviceHandler.receivedResponse(nwkAddrOfInterest, ResponseType.NODE_DESCRIPTOR_RESPONSE);
				synchronized (deviceHandlerLock) {
					deviceHandlerLock.notify();
				}
			}
			else if (isDeviceAddress(sourceAddress64Bit) && isDeviceAddress(sourceAddress16Bit)) {
				localDevice.addRemoteDevice(sourceAddress64Bit, nwkAddrOfInterest);
				synchronized (deviceHandlerLock) {
					deviceHandlerLock.notify();
				}
			}
			localDevice.getDevices().get(sourceAddress64Bit).nodeIdentifier = "Unknown_"
					+ Long.toHexString(sourceAddress64Bit).toUpperCase();

			return;
		}

		nwkAddrOfInterest = Short.reverseBytes(byteBuffer.getShort()); // Network
		// address
		// of
		// interest

		byte[] rawNodeDescriptor = new byte[byteBuffer.limit() - byteBuffer.position()];
		byteBuffer.get(rawNodeDescriptor);
		NodeDescriptor nodeDescriptor = new NodeDescriptor();
		nodeDescriptor.setRawNodeDescriptor(rawNodeDescriptor);
		nodeDescriptor.parseRawNodeDescriptor();

		switch (nwkAddrOfInterest) {
		case (short) 0xFFFF: // Broadcast to all devices in PAN
		case (short) 0xFFFE: // Reserved broadcast
		case (short) 0xFFFD: // Broadcast to macRxOnWhenIdle = TRUE
		case (short) 0xFFFC: // Broadcast to all routers and coordinators
		case (short) 0xFFFB: // Broadcast to low power routers only
		case (short) 0xFFFA: // Reserved
		case (short) 0xFFF9: // Reserved
		case (short) 0xFFF8: // Reserved
			nwkAddrOfInterest = sourceAddress16Bit;
			break;
		}

		if (nwkAddrOfInterest == 0x0000) {
			return;
		}
		else if (localDevice.isKnownDevice(nwkAddrOfInterest)) {
			localDevice.getRemoteDevice(nwkAddrOfInterest).setNodeDescriptor(nodeDescriptor);
			deviceHandler.receivedResponse(nwkAddrOfInterest, ResponseType.NODE_DESCRIPTOR_RESPONSE);
			synchronized (deviceHandlerLock) {
				deviceHandlerLock.notify();
			}
		}
		else if (isDeviceAddress(sourceAddress64Bit) && isDeviceAddress(sourceAddress16Bit)) {
			localDevice.addRemoteDevice(sourceAddress64Bit, nwkAddrOfInterest);
			synchronized (deviceHandlerLock) {
				deviceHandlerLock.notify();
			}
		}
	}

	private void handleSimpleDescriptorResponse() {
		byteBuffer.get(); // Omit frame control byte
		if (Constants.STATUS_SUCCESS != byteBuffer.get())
			return; // No success
		nwkAddrOfInterest = Short.reverseBytes(byteBuffer.getShort()); // Network
		// address
		// of
		// interest
		byte[] rawSimpleDescriptor = new byte[byteBuffer.limit() - byteBuffer.position()];
		byteBuffer.get(rawSimpleDescriptor);
		SimpleDescriptor simpleDescriptor = new SimpleDescriptor();
		simpleDescriptor.setRawSimpleDescriptor(rawSimpleDescriptor);
		simpleDescriptor.parseRawSimpleDescriptor();

		switch (nwkAddrOfInterest) {
		case (short) 0xFFFF: // Broadcast to all devices in PAN
		case (short) 0xFFFE: // Reserved broadcast
		case (short) 0xFFFD: // Broadcast to macRxOnWhenIdle = TRUE
		case (short) 0xFFFC: // Broadcast to all routers and coordinators
		case (short) 0xFFFB: // Broadcast to low power routers only
		case (short) 0xFFFA: // Reserved
		case (short) 0xFFF9: // Reserved
		case (short) 0xFFF8: // Reserved
			nwkAddrOfInterest = sourceAddress16Bit;
			break;
		}
		RemoteDevice tempDevice = localDevice.getRemoteDevice(nwkAddrOfInterest);
		if (tempDevice == null)
			return;
		if (simpleDescriptor.getApplicationProfileId() == (short) (0xC105))
			return; // There is noting to do for XBee devices in relation to simple descriptor.
		logger.debug("nwkAddrOfInterest: " + Integer.toHexString(nwkAddrOfInterest & 0xffff));
		logger.debug("EndpointId: " + Constants.bytesToHex(simpleDescriptor.getEndpointId()));
		Endpoint tempEndpoint = tempDevice.getEndpoint(simpleDescriptor.getEndpointId());
		tempEndpoint.setSimpleDescriptor(simpleDescriptor);
		tempEndpoint.parseClusters();
		deviceHandler.receivedResponse(nwkAddrOfInterest, ResponseType.SIMPLE_DESCRIPTOR_RESPONSE);
		synchronized (deviceHandlerLock) {
			deviceHandlerLock.notify();
		}
	}

	private void handleActiveEndpointResponse() {
		byteBuffer.get(); // Omit frame control byte
		if (Constants.STATUS_SUCCESS != byteBuffer.get())
			return; // No success
		nwkAddrOfInterest = Short.reverseBytes(byteBuffer.getShort()); // Network
		// address
		// of
		// interest
		int numberOfEndpoints = (byteBuffer.get() & 0xff);
		for (int i = 0; i < numberOfEndpoints; ++i) { // Iterate through found
			// endpoints
			byte endpointId = byteBuffer.get();
			localDevice.getRemoteDevice(nwkAddrOfInterest).getEndpoints().put(endpointId,
					new Endpoint(localDevice.getRemoteDevice(nwkAddrOfInterest), endpointId));
			simpleDescriptorRequest.setNwkAddrOfInterest(nwkAddrOfInterest);
			simpleDescriptorRequest.setEndpoint(endpointId);
			try {
				localDevice.sendFrame(XBeeFrameFactory.composeMessageToFrame(simpleDescriptorRequest.getUnicastMessage(
						localDevice.getRemoteDevice(nwkAddrOfInterest).getAddress64Bit(), nwkAddrOfInterest)));
			} catch (WrongFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		deviceHandler.receivedResponse(nwkAddrOfInterest, ResponseType.ACTIVE_ENDPOINT_RESPONSE);
		synchronized (deviceHandlerLock) {
			deviceHandlerLock.notify();
		}
	}

	private void handleDeviceAnnce() {
		byteBuffer.get(); // Omit frame control byte
		address16Bit = Short.reverseBytes(byteBuffer.getShort());
		address64Bit = Long.reverseBytes(byteBuffer.getLong());
		byteBuffer.get(); // Omit capability byte, not needed at the moment
		if (!localDevice.isKnownDevice(address64Bit) && isDeviceAddress(address64Bit) && isDeviceAddress(address16Bit)) {

			// changed
			localDevice.addRemoteDevice(address64Bit, address16Bit);
			synchronized (deviceHandlerLock) {
				deviceHandlerLock.notify();
			}
		}
	}

	/**
	 * Check if the address is a valid device address.
	 * 
	 * @param address16Bit
	 * @return false if broadcast, coordinator or reserved address
	 */
	private boolean isDeviceAddress(short address16Bit) {
		switch (address16Bit) {
		case (short) 0xFFFF: // Broadcast to all devices in PAN
		case (short) 0xFFFE: // Reserved broadcast
		case (short) 0xFFFD: // Broadcast to macRxOnWhenIdle = TRUE
		case (short) 0xFFFC: // Broadcast to all routers and coordinators
		case (short) 0xFFFB: // Broadcast to low power routers only
		case (short) 0xFFFA: // Reserved
		case (short) 0xFFF9: // Reserved
		case (short) 0xFFF8: // Reserved
		case 0x0000: // Coordinator
			return false;
		}
		return true;
	}

	/**
	 * Check if the address is a valid device address.
	 * 
	 * @param address64Bit
	 * @return false if broadcast or coordinator address
	 */
	private boolean isDeviceAddress(long address64Bit) {
		if (0x000000000000FFFF == address64Bit || 0x0000000000000000 == address64Bit) {
			return false;
		}
		return true;
	}
}
