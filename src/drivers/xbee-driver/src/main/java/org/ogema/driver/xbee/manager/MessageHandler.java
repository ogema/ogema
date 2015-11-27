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
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.ogema.driver.xbee.Configuration;
import org.ogema.driver.xbee.Constants;
import org.ogema.driver.xbee.manager.zcl.Cluster;
import org.slf4j.Logger;

/**
 * This class handles outgoing messages that need a response.
 * 
 * @author puschas
 * 
 */
public class MessageHandler implements Runnable {
	private volatile boolean running;
	private LocalDevice localDevice;
	// private Thread timerThread;
	protected final Object messageHandlerLock = new Object();
	public long MIN_WAITING_TIME;
	private int cyclicSleepPeriod;
	private volatile Map<Long, SentMessage> sentMessagesAwaitingResponse; // Only
	// for
	// unicast
	// frames
	// to
	// specific
	// devices
	private final Logger logger = org.slf4j.LoggerFactory.getLogger("xbee-driver");

	public MessageHandler(LocalDevice device, int cyclicSleepPeriod) {
		running = true;
		sentMessagesAwaitingResponse = new ConcurrentHashMap<Long, SentMessage>();
		this.localDevice = device;
		setCyclicSleepPeriod(cyclicSleepPeriod);
	}

	/**
	 * 
	 * @param running
	 *            Let's the loop in run() finish and then exit.
	 */
	public void stop() {
		this.running = false;
	}

	@Override
	public void run() {
		while (running) {
			synchronized (messageHandlerLock) {
				try {
					messageHandlerLock.wait(MIN_WAITING_TIME);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			Iterator<Map.Entry<Long, SentMessage>> iter = sentMessagesAwaitingResponse.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry<Long, SentMessage> entry = iter.next();
				if (entry.getValue().getTimeDifference() > (MIN_WAITING_TIME / 1000)) {
					synchronized (entry.getValue().getChannelLock()) {
						entry.getValue().getChannelLock().notifyAll(); // Notify waiting
						// objects
						// TODO let them know it failed?
					}
					logger.info("Warning: expected response not received, removing sent message via iterator");
					iter.remove(); // Remove failed frame from list
				}
			}
		}
	}

	/**
	 * Parses the received message payload
	 * 
	 * @param remoteEndpoint
	 * @param localEndpoint
	 * @param profileId
	 * @param clusterId
	 * @param payload
	 */
	public void messageReceived(Endpoint remoteEndpoint, byte localEndpoint, short clusterId,
			ByteBuffer payloadBuffer/* byte[] payload */) {
		if (Configuration.DEBUG)
			logger.debug("messageReceived");
		if (remoteEndpoint == null) {
			if (Configuration.DEBUG)
				logger.info("Error, endpoint not found"); // TODO handle error
															// (throw exception
															// etc.)
			return;
		}
		byte frameControl = payloadBuffer.get();
		if ((frameControl & 0B00000011) == 0) { // profile-wide
			if ((frameControl & 0B00000100) == 0B00000100) { // manufacturer specific
				payloadBuffer.getShort(); // Manufacturer Code, not needed at the moment
			}
			payloadBuffer.get(); // sequence number
			byte command = payloadBuffer.get();

			long addr64 = remoteEndpoint.getDevice().getAddress64Bit();
			Cluster cluster = remoteEndpoint.getClusters().get(clusterId);
			switch (command) {
			case Constants.WRITE_ATTRIBUTES_RESPONSE:
			case Constants.READ_ATTRIBUTES_RESPONSE:
				if (Configuration.DEBUG)
					logger.debug("READ_ATTRIBUTES_RESPONSE");
				if (sentMessagesAwaitingResponse.containsKey(addr64)) {
					SentMessage sentMessage = sentMessagesAwaitingResponse.get(addr64);
					if (command == sentMessage.getResponseType()) {
						cluster.readMessage(payloadBuffer, command, sentMessage.getChannelLock());
						sentMessagesAwaitingResponse.remove(addr64);
						return;
					}
				}
				cluster.readMessage(payloadBuffer, command);
				break;
			case Constants.DEFAULT_RESPONSE:
				command = payloadBuffer.get(); // The sent command that was sent
												// to the remote device
				byte status = payloadBuffer.get();
				if (Constants.UNSUPPORTED_CLUSTER_COMMAND == status) {
					if (sentMessagesAwaitingResponse.containsKey(addr64)) {
						sentMessagesAwaitingResponse.remove(addr64);
					}
				}
				break;
			case Constants.READ_ATTRIBUTES:
				localDevice.readAttributes(remoteEndpoint, localEndpoint, clusterId, payloadBuffer);
				break;
			}
		}
		else {
			logger.info("***** not profile wide command");
		}
	}

	/**
	 * Send a message and keep the message in a map. It will be removed from the map if the answer arrived, a failure
	 * was detected or it timed out. The channelLock will be notified after then.
	 * 
	 * @param message
	 * @param responseType
	 * @param channelLock
	 */
	public void sendMessage(byte[] message, byte responseType, Object channelLock) {
		if (Configuration.DEBUG)
			logger.debug("sendMessage");
		message[1] = localDevice.getFrameId(); // Overwrite the placeholder frameId
		ByteBuffer bb = ByteBuffer.wrap(message);
		bb.position(2);
		long destinationAddress64Bit = bb.getLong();
		SentMessage messageToSend = new SentMessage(message, responseType, channelLock);
		messageToSend.refreshTimestamp();
		sentMessagesAwaitingResponse.put(destinationAddress64Bit, messageToSend);
		localDevice.sendMessage(messageToSend.getMessage());
	}

	void printTxString(byte frameId, byte deliveryStatus, byte discoveryStatus) {
		StringBuilder txString = new StringBuilder();
		txString.append("\n\n############################################");
		txString.append("\ntxStatusReceived");
		txString.append("\nTX frame ID: " + Constants.bytesToHex(frameId));
		txString.append("\nTX status: " + Constants.bytesToHex(deliveryStatus));
		txString.append("\nTX discovery status: " + Constants.bytesToHex(discoveryStatus));
		txString.append("\n############################################\n\n");
		logger.debug(txString.toString());

	}

	public void txStatusReceived(ByteBuffer byteBuffer) { // Transmit status
		if (byteBuffer.position() == byteBuffer.limit()) {
			logger.debug("txStatus empty");
			return;
		}
		byte frameId = byteBuffer.get(); // omit frameId
		short destination = byteBuffer.getShort();
		byteBuffer.get(); // omit tx retry count
		byte deliveryStatus = byteBuffer.get();
		byte discoveryStatus = byteBuffer.get();
		// TODO Remove device or just set offline? What about Channels-Device?
		long address64Bit = localDevice.getDestinationFromFrameId(frameId);
		if (address64Bit == 0) {
			return;
		}
		if (deliveryStatus != Constants.STATUS_SUCCESS) {
			SentMessage sentMessage = sentMessagesAwaitingResponse.get(address64Bit);
			if (null != sentMessage) {
				if (sentMessage.getFrameId() == frameId) {
					sentMessage.getChannelLock().notify();
					sentMessagesAwaitingResponse.remove(address64Bit);
				}
			}
		}
		switch (deliveryStatus) {
		case Constants.STATUS_SUCCESS: // success
			return;
		case 0x24: // address not found
			switch (discoveryStatus) {
			case 0x02:
				logger.debug("Route discovery");
				return;
			case 0x03:
			case 0x40:
				logger.debug("\nTX discovery status: " + Constants.bytesToHex(discoveryStatus));
				return;
			case 0x00:
				logger.debug("No Overhead");
			}
			logger.info("remove frame");
			logger.info("remove device with address :" + Long.toHexString(address64Bit));
			// TODO send request for 16bit address in case it just changed?
			localDevice.removeRemoteDevice(address64Bit);

			localDevice.getDeviceHandler().removeFromSentFramesAwaitingResponse(address64Bit);
			// TODO what about channels?
			break;
		case 0x02: // cca failure
		case 0x15: // invalid destination endpoint
		case 0x21: // network ack failure
		case 0x22: // not join to network
			logger.debug("not join to network");
		case 0x23: // self-adressed
		case 0x25: // route not found
			logger.debug("Route not found");
		case 0x74: // data payload too large
			break;
		}

		SentMessage sentMessage = sentMessagesAwaitingResponse.get(destination);
		if (sentMessage != null) {
			if (sentMessage.getChannelLock() != null) {
				synchronized (sentMessage.getChannelLock()) {
					sentMessage.getChannelLock().notify(); // Notify waiting objects
					// TODO let them know it failed? How?
				}
			}
			logger.warn("removing sent message after timeout");
			sentMessagesAwaitingResponse.remove(destination); // Remove failed
			// frame from
			// list
		}
		// txString.append("\n"+Constants.bytesToHex(localDevice.frameIdMap.get(frameId))); // can lead to exception //
		// NI request had no frame id
		if (Configuration.DEBUG)
			printTxString(frameId, deliveryStatus, discoveryStatus);
	}

	public int getCyclicSleepPeriod() {
		return cyclicSleepPeriod;
	}

	/**
	 * 
	 * @param cyclicSleepPeriod
	 *            in milliseconds
	 */
	public void setCyclicSleepPeriod(int cyclicSleepPeriod) {
		this.cyclicSleepPeriod = cyclicSleepPeriod;
		MIN_WAITING_TIME = cyclicSleepPeriod + 2000;
		logger.debug("min waiting time: " + MIN_WAITING_TIME);
	}
}
