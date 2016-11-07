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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.ogema.driver.xbee.frames.RemoteAtCommand;
import org.ogema.driver.xbee.frames.WrongFormatException;
import org.ogema.driver.xbee.manager.InputHandler.ResponseType;
import org.ogema.driver.xbee.manager.RemoteDevice.DeviceStates;
import org.ogema.driver.xbee.manager.RemoteDevice.InitStates;
import org.ogema.driver.xbee.manager.zdo.ActiveEndpointsRequest;
import org.ogema.driver.xbee.manager.zdo.MgmtLqiRequest;
import org.ogema.driver.xbee.manager.zdo.NetworkAddressRequest;
import org.ogema.driver.xbee.manager.zdo.NodeDescriptorRequest;
import org.ogema.driver.xbee.manager.zdo.SimpleDescriptorRequest;
import org.ogema.driver.xbee.manager.zdo.UserDescriptorRequest;
import org.slf4j.Logger;

/**
 * This class checks the states of all remote devices and makes sure they get initialized. It also resends frames that
 * have not received a response after a certain amount of time.
 * 
 * @author puschas
 * 
 */
public class DeviceHandler implements Runnable {
	private volatile boolean running;
	private LocalDevice localDevice;
	private volatile Map<Long, SentFrame> sentFramesAwaitingResponse; // Only
	// for
	// unicast
	// frames
	// to
	// specific
	// devices
	private SentFrame sentFrame;
	private final int RETRY_LIMIT = 3;
	private long MIN_WAITING_TIME;
	// private Thread timerThread;
	protected final Object deviceHandlerLock = new Object();
	private final Logger logger = org.slf4j.LoggerFactory.getLogger("xbee-driver");
	private int cyclicSleepPeriod;

	public DeviceHandler(LocalDevice localDevice, int cyclicSleepPeriod) {
		running = true;
		sentFramesAwaitingResponse = new ConcurrentHashMap<Long, SentFrame>();
		this.localDevice = localDevice;
		setCyclicSleepPeriod(cyclicSleepPeriod);

		/*
		 * This thread notifies the endpoint handler after a certain amount of time.
		 */
		// timerThread = new Thread(new Runnable() {
		// public void run() {
		// while (true) {
		// try {
		// Thread.sleep(MIN_WAITING_TIME);
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// synchronized (deviceHandlerLock) {
		// deviceHandlerLock.notify();
		// }
		// }
		// }
		// });
		// timerThread.setName("xbee-lld-deviceHandlerTimer");
		// timerThread.start();
	}

	public Object getDeviceHandlerLock() {
		return deviceHandlerLock;
	}

	/**
	 * This method is called by the inputHandler once a response is received. It checks if a endpoint was waiting for
	 * this request and yes it removes the frame from the sentFramesAwaitingResponse map and sets the endpoint status to
	 * idle.
	 * 
	 * @param address64Bit
	 * @param responseType
	 */
	public void receivedResponse(short address16Bit, ResponseType responseType) {
		long address64Bit = localDevice.get64BitAddress(address16Bit);
		logger.debug("***receivedResponse***");
		if (sentFramesAwaitingResponse.isEmpty())
			return;
		logger.debug("***receivedResponse NOT EMPTY***");
		logger.debug("***Response type: " + responseType.toString() + "***");
		if (!sentFramesAwaitingResponse.containsKey(address64Bit)) {
			logger.debug("***Not found in sentFramesAwaitingResponse map***");
			return;
		}
		if (responseType.equals(sentFramesAwaitingResponse.get(address64Bit).getResponseType())) {
			logger.debug("***Remove response");
			sentFramesAwaitingResponse.remove(address64Bit);
			localDevice.getRemoteDevice(address64Bit).setDeviceState(DeviceStates.IDLE);
		}

	}

	public void receivedResponse(long address64Bit, ResponseType responseType) {
		logger.debug("***receivedResponse***");
		if (sentFramesAwaitingResponse.isEmpty())
			return;
		logger.debug("***receivedResponse NOT EMPTY***");
		logger.debug("***Response type: " + responseType.toString() + "***");
		if (!sentFramesAwaitingResponse.containsKey(address64Bit)) {
			logger.debug("***Not found in sentFramesAwaitingResponse map***");
			return;
		}
		if (responseType.equals(sentFramesAwaitingResponse.get(address64Bit).getResponseType())) {
			logger.debug("***Remove response");
			sentFramesAwaitingResponse.remove(address64Bit);
			localDevice.getRemoteDevice(address64Bit).setDeviceState(DeviceStates.IDLE);
		}
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
			synchronized (deviceHandlerLock) {
				try {
					deviceHandlerLock.wait(MIN_WAITING_TIME);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			loop: for (Map.Entry<Long, RemoteDevice> device : localDevice.getDevices().entrySet()) {
				RemoteDevice remoteDevice = device.getValue();
				switch (remoteDevice.getDeviceState()) {
				case AWAITING_RESPONSE: // Waiting for the response of a request
					sentFrame = sentFramesAwaitingResponse.get(remoteDevice.getAddress64Bit());
					if (sentFrame == null)
						break;
					logger.debug("awaiting response: " + sentFrame.getResponseType().toString());
					logger.debug("from: " + Long.toHexString(remoteDevice.getAddress64Bit()));
					logger.debug("from: " + Integer.toHexString(remoteDevice.getAddress16Bit() & 0xffff));
					logger.debug("Number of retries: " + sentFrame.getNumberOfRetries() + " > " + RETRY_LIMIT);
					if (sentFrame.getNumberOfRetries() > RETRY_LIMIT) { // No
						// more
						// retries
						// for
						// now
						logger.debug("too many retries");
						sentFramesAwaitingResponse.remove(remoteDevice.getAddress64Bit());
						localDevice.removeRemoteDevice(remoteDevice.getAddress16Bit()); // Device seems
						// unreachable,
						// remove it
						// from map
					}
					else if (sentFrame.getTimeDifference() > (MIN_WAITING_TIME / 1000)) { // Waited
						// long
						// enough,
						// now
						// retry
						logger.debug("resend frame");
						localDevice.sendFrame(sentFrame.getFrame());
						sentFrame.refreshTimestamp();
						sentFrame.incrementNumberOfRetires();
					}
					continue loop; // Don't enter the next switch.
				case IDLE:
					// Not waiting for any responses, it is save to check
					// initState and send requests
					break;
				}
				switch (remoteDevice.getInitState()) {
				case INITIALIZED: // The endpoint is idle and initialized so
					// there is nothing to do
					break;
				case UNINITIALIZED: // The endpoint is missing some information
					if (remoteDevice.getAddress16Bit() == 0) {
						logger.debug("The network address is missing");
						NetworkAddressRequest networkAddressRequest = new NetworkAddressRequest(
								remoteDevice.getAddress64Bit());
						byte frameId = localDevice.getFrameId();
						networkAddressRequest.setFrameId(frameId);
						byte[] tempFrame = null;
						try {
							tempFrame = XBeeFrameFactory.composeMessageToFrame(networkAddressRequest
									.getUnicastMessageSingleResponse(remoteDevice.getAddress64Bit()));
						} catch (WrongFormatException e) {
							e.printStackTrace();
						}
						logger.debug("\n\n\n\n\nSending network address request to: ");
						logger.debug(Long.toHexString(remoteDevice.getAddress64Bit()));
						// TODO localDevice.frameIdMap.put(frameId, tempFrame);
						localDevice.sendFrame(tempFrame);
						sentFramesAwaitingResponse.put(remoteDevice.getAddress64Bit(),
								new SentFrame(tempFrame, ResponseType.NETWORK_ADDRESS_RESPONSE));
						localDevice.setFrameIdToDestination(frameId, remoteDevice.getAddress64Bit());
						remoteDevice.setDeviceState(DeviceStates.AWAITING_RESPONSE);
					}

					if (remoteDevice.getEndpoints().isEmpty()) { // The
						// endpoints
						// are
						// missing
						logger.debug("The endpoints are missing: ");
						ActiveEndpointsRequest activeEndpointsRequest = new ActiveEndpointsRequest();
						activeEndpointsRequest.setNwkAddrOfInterest(remoteDevice.getAddress16Bit());
						byte frameId = localDevice.getFrameId();
						activeEndpointsRequest.setFrameId(frameId);
						byte[] tempFrame = null;
						try {
							tempFrame = XBeeFrameFactory.composeMessageToFrame(activeEndpointsRequest
									.getUnicastMessage(remoteDevice.getAddress64Bit(), remoteDevice.getAddress16Bit()));
						} catch (WrongFormatException e) {
							e.printStackTrace();
						}
						logger.debug("\n\n\n\n\nSending active endpoints request to: ");
						logger.debug(Long.toHexString(remoteDevice.getAddress64Bit()));
						logger.debug(Integer.toHexString(remoteDevice.getAddress16Bit() & 0xffff));
						// TODO localDevice.frameIdMap.put(frameId, tempFrame);
						localDevice.sendFrame(tempFrame);
						sentFramesAwaitingResponse.put(remoteDevice.getAddress64Bit(),
								new SentFrame(tempFrame, ResponseType.ACTIVE_ENDPOINT_RESPONSE));
						localDevice.setFrameIdToDestination(frameId, remoteDevice.getAddress64Bit());
						remoteDevice.setDeviceState(DeviceStates.AWAITING_RESPONSE);
					}
					else if (null == remoteDevice.getNodeDescriptor()) { // The
						// node
						// descriptor
						// is
						// missing
						logger.debug("The node descriptor is missing: ");
						NodeDescriptorRequest nodeDescriptorRequest = new NodeDescriptorRequest();
						nodeDescriptorRequest.setNwkAddrOfInterest(remoteDevice.getAddress16Bit());
						byte frameId = localDevice.getFrameId();
						nodeDescriptorRequest.setFrameId(frameId);
						byte[] tempFrame = null;
						try {
							tempFrame = XBeeFrameFactory.composeMessageToFrame(nodeDescriptorRequest
									.getUnicastMessage(remoteDevice.getAddress64Bit(), remoteDevice.getAddress16Bit()));
						} catch (WrongFormatException e) {
							e.printStackTrace();
						}
						// TODO localDevice.frameIdMap.put(frameId, tempFrame);
						localDevice.sendFrame(tempFrame);
						sentFramesAwaitingResponse.put(remoteDevice.getAddress64Bit(),
								new SentFrame(tempFrame, ResponseType.NODE_DESCRIPTOR_RESPONSE));
						localDevice.setFrameIdToDestination(frameId, remoteDevice.getAddress64Bit());
						remoteDevice.setDeviceState(DeviceStates.AWAITING_RESPONSE);
					}
					else if (remoteDevice.getNodeDescriptor().hasUserDescriptor()
							&& null == remoteDevice.getUserDescriptor()) {
						logger.debug("The user descriptor is missing: ");
						UserDescriptorRequest userDescriptorRequest = new UserDescriptorRequest();
						userDescriptorRequest.setNwkAddrOfInterest(remoteDevice.getAddress16Bit());
						byte frameId = localDevice.getFrameId();
						userDescriptorRequest.setFrameId(frameId);
						byte[] tempFrame = null;
						try {
							tempFrame = XBeeFrameFactory.composeMessageToFrame(userDescriptorRequest
									.getUnicastMessage(remoteDevice.getAddress64Bit(), remoteDevice.getAddress16Bit()));
						} catch (WrongFormatException e) {
							e.printStackTrace();
						}
						localDevice.sendFrame(tempFrame);
						sentFramesAwaitingResponse.put(remoteDevice.getAddress64Bit(),
								new SentFrame(tempFrame, ResponseType.USER_DESCRIPTOR_RESPONSE));
						localDevice.setFrameIdToDestination(frameId, remoteDevice.getAddress64Bit());
						remoteDevice.setDeviceState(DeviceStates.AWAITING_RESPONSE);
					} /*
						 * else if (remoteDevice.getNodeDescriptor() // TODO need to implement
						 * handleComplexDescriptorResponse in InputHandler first .hasComplexDescriptor() && null ==
						 * remoteDevice.getComplexDescriptor()) { logger.info("The complex descriptor is missing: ");
						 * ComplexDescriptorRequest complexDescriptorRequest = new ComplexDescriptorRequest();
						 * complexDescriptorRequest .setNwkAddrOfInterest(remoteDevice .getAddress16Bit()); byte[]
						 * tempFrame = null; byte frameId = localDevice.getFrameId();
						 * complexDescriptorRequest.setFrameId(frameId); try { tempFrame =
						 * XBeeFrameFactory.composeMessageToFrame( complexDescriptorRequest.getUnicastMessage(
						 * remoteDevice .getAddress64Bit(), remoteDevice .getAddress16Bit())); } catch
						 * (WrongFormatException e) { // TODO Auto-generated catch block e.printStackTrace(); }
						 * localDevice.sendFrame(tempFrame); sentFramesAwaitingResponse.put(remoteDevice
						 * .getAddress64Bit(), new SentFrame(tempFrame, ResponseType.COMPLEX_DESCRIPTOR_RESPONSE));
						 * remoteDevice .setDeviceState(DeviceStates.AWAITING_RESPONSE); }
						 */
					else if (remoteDevice instanceof XBeeDevice
							&& null == ((XBeeDevice) remoteDevice).getNodeIdentifier()) {

						logger.debug("The NI is missing: ");
						RemoteAtCommand niRequest = new RemoteAtCommand(localDevice.getFrameId(),
								remoteDevice.getAddress64Bit(), remoteDevice.getAddress16Bit(), (byte) 0x00,
								(short) 0x4E49, null);
						byte[] tempFrame = null;
						try {
							tempFrame = XBeeFrameFactory.composeMessageToFrame(niRequest.getMessage());
						} catch (WrongFormatException e) {
							e.printStackTrace();
						}
						localDevice.sendFrame(tempFrame);
						sentFramesAwaitingResponse.put(remoteDevice.getAddress64Bit(),
								new SentFrame(tempFrame, ResponseType.REMOTE_NI_COMMAND));
						remoteDevice.setDeviceState(DeviceStates.AWAITING_RESPONSE);
					}
					else { // Check if all active endpoints have there simple
							// descriptor, if not request it for one
							// endpoint at a time.
						logger.debug("Check if all active endpoints have their simple descriptor: ");
						for (Map.Entry<Byte, Endpoint> endpoint : remoteDevice.getEndpoints().entrySet()) {
							if (!(remoteDevice instanceof XBeeDevice)
									&& null == endpoint.getValue().getSimpleDescriptor()) {
								SimpleDescriptorRequest simpleDescriptorRequest = new SimpleDescriptorRequest();
								simpleDescriptorRequest.setNwkAddrOfInterest(remoteDevice.getAddress16Bit());
								simpleDescriptorRequest.setEndpoint(endpoint.getKey());
								byte frameId = localDevice.getFrameId();
								simpleDescriptorRequest.setFrameId(frameId);
								byte[] tempFrame = null;
								try {
									tempFrame = XBeeFrameFactory.composeMessageToFrame(
											simpleDescriptorRequest.getUnicastMessage(remoteDevice.getAddress64Bit(),
													remoteDevice.getAddress16Bit()));
								} catch (WrongFormatException e) {
									e.printStackTrace();
								}
								localDevice.sendFrame(tempFrame);
								sentFramesAwaitingResponse.put(remoteDevice.getAddress64Bit(),
										new SentFrame(tempFrame, ResponseType.SIMPLE_DESCRIPTOR_RESPONSE));
								localDevice.setFrameIdToDestination(frameId, remoteDevice.getAddress64Bit());
								remoteDevice.setDeviceState(DeviceStates.AWAITING_RESPONSE);
								break;
							}
						}
						if (DeviceStates.AWAITING_RESPONSE != remoteDevice.getDeviceState()) {
							remoteDevice.setInitState(InitStates.INITIALIZED);
							logger.info("-------Device initlialized-------");
							logger.info("64Bit Address:" + Long.toHexString(remoteDevice.getAddress64Bit()));
							logger.info(
									"16Bit Address:" + Integer.toHexString(remoteDevice.getAddress16Bit() & 0xffff));
							remoteDevice.postInit();
							/*
							 * logger.info("Endpoints: "); for (Map.Entry<Byte, Endpoint> endpoint : remoteDevice
							 * .getEndpoints().entrySet()) { logger.info("  Endpoint: " +
							 * Integer.toHexString(endpoint.getKey() & 0xff)); for (Map.Entry<Short, Cluster> cluster :
							 * endpoint .getValue().getClusters().entrySet()) { logger.info("    Cluster: " +
							 * Integer.toHexString(cluster .getKey() & 0xffff)); for (Map.Entry<Short, ClusterAttribute>
							 * clusterAttribute : cluster .getValue().clusterAttributes .entrySet()) { logger.info(
							 * "      Cluster Attribute: " + Integer .toHexString(clusterAttribute .getKey() & 0xffff) +
							 * " " + clusterAttribute.getValue() .getAttributeName()); } for (Entry<Byte,
							 * ClusterCommand> clusterCommand : cluster .getValue().clusterCommands .entrySet()) {
							 * logger.info("      Cluster Command: " + Integer .toHexString(clusterCommand .getKey() &
							 * 0xff) + " " + clusterCommand.getValue() .getDescription()); } } }
							 */
						}
					}
					break;
				}
			}
		}
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
		MIN_WAITING_TIME = cyclicSleepPeriod + 2000; // wait 2 seconds after the csp is over before sending again
	}

	// This method scans the network to find new devices by sending a Neighbor Table Request to the coordinator and then
	// subsequently to all found routers. It will also send a broadcast Node Descriptor Request.
	public void initNetworkScan() {
		// TODO Mgmt_Rtg_req
		MgmtLqiRequest mgmtLqiRequest = new MgmtLqiRequest();
		try {
			localDevice.sendFrame(XBeeFrameFactory.composeMessageToFrame(mgmtLqiRequest.getCoordinatorMessage()));
		} catch (WrongFormatException e) {
			e.printStackTrace();
		}

		// This is needed because sometimes devices don't seem to respond with their neighbour table
		NodeDescriptorRequest nodeDescriptorRequest = new NodeDescriptorRequest();
		nodeDescriptorRequest.setNwkAddrOfInterest((short) 0xFFFE); // unknown/broadcast
		try {
			localDevice.sendFrame(XBeeFrameFactory.composeMessageToFrame(nodeDescriptorRequest.getBroadcastMessage()));
		} catch (WrongFormatException e) {
			e.printStackTrace();
		}
	}

	public void removeFromSentFramesAwaitingResponse(long address64Bit) {
		sentFramesAwaitingResponse.remove(address64Bit);
	}
}
