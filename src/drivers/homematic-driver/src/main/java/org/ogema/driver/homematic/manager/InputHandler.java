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
package org.ogema.driver.homematic.manager;

import java.util.Arrays;

import org.ogema.driver.homematic.Activator;
import org.ogema.driver.homematic.manager.RemoteDevice.InitStates;
import org.ogema.driver.homematic.tools.Converter;
import org.slf4j.Logger;

/**
 * Handles incoming messages and responds accordingly.
 * 
 * @author baerthbn
 * 
 */
public class InputHandler implements Runnable {

	private volatile boolean running;
	private volatile Object inputEventLock;
	private MessageHandler messageHandler;
	private LocalDevice localDevice;
	private StatusMessage lastMsg = new StatusMessage();

	private final Logger logger = org.slf4j.LoggerFactory.getLogger("homematic-driver");
	private boolean localDeviceInited;

	public InputHandler(LocalDevice localDevice) {
		inputEventLock = localDevice.getInputEventLock();
		messageHandler = localDevice.getMessageHandler();
		running = true;
		this.localDevice = localDevice;
	}

	/**
	 * 
	 *            Stops the loop in run().
	 */
	public void stop() {
		this.running = false;
	}

	@Override
	public void run() {
		while (running && Activator.bundleIsRunning) {
			synchronized (inputEventLock) {
				while (!localDevice.connectionHasFrames()) {
					try {
						inputEventLock.wait();
					} catch (InterruptedException e1) {
						// e1.printStackTrace();
						if (!Activator.bundleIsRunning)
							return;
					}
				}
				// long timeStamp = System.currentTimeMillis();
				// System.out.print("receive: ");
				// System.out.println(timeStamp);

				try {
					handleMessage(localDevice.getReceivedFrame());
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		}
	}

	private void handleMessage(byte[] tempArray) {
		logger.debug("message tpye: " + (char) tempArray[0]);
		switch (tempArray[0]) {
		case 'H':
			if (!localDeviceInited)
				parseAdapterMsg(tempArray);
			break;
		case 'R':
		case 'E':
			StatusMessage emsg = new StatusMessage(tempArray);
//			if (!emsg.almostEquals(lastMsg)) {
				if (emsg.msg_type == 0x00 & localDevice.getPairing() != null) { // if pairing
					logger.debug("Pairing response received");
					RemoteDevice temp_device = new RemoteDevice(localDevice, emsg);
					if (localDevice.getPairing().equals("0000000000")
							| localDevice.getPairing().equals(temp_device.getSerial())) {

						RemoteDevice found_device = localDevice.getDevices().get(temp_device.getAddress());
						if (found_device == null) {
							localDevice.getDevices().put(temp_device.getAddress(), temp_device);
							temp_device.init();
						}
						else if (found_device.getInitState().equals(InitStates.UNKNOWN)) {
							temp_device = localDevice.getDevices().get(found_device.getAddress());
							temp_device.init();
						}
					}
				}
				else {
					if (localDevice.getOwnerid().equals(emsg.destination) || emsg.destination.equals("000000")
							|| emsg.partyMode) {
						if (localDevice.getDevices().containsKey(emsg.source)) {
							// 000000 = broadcast
							logger.debug("InputHandler has device");
							messageHandler.messageReceived(emsg);
						}
						else
							logger.debug("Unpaired Homematic device detected: " + emsg.source);
					}
				}
//			}
//			else
//				logger.debug("Message is equal to the last message!");
			lastMsg = emsg;
			break;
		case 'I':
			// This is needed for AES magic
			break;
		default:
			// TODO: Status & Condition Handling !
		}
	}

	private void parseAdapterMsg(byte[] data) {
		localDevice.setName(new String(Arrays.copyOfRange(data, 2, 11)));
		long raw_version = Converter.toLong(data, 11, 2);
		localDevice.setFirmware(String.format("%d.%d", (raw_version >> 12) & 0xf, raw_version & 0xffff));
		localDevice.setSerial(new String(Arrays.copyOfRange(data, 14, 24)));
		String ownerid = Converter.toHexString(data, 27, 3);
		if (ownerid.equals("000000"))
			ownerid = Converter.toHexString(data, 24, 3);
		localDevice.setOwnerid(ownerid);
		localDevice.setUptime((int) Converter.toLong(data, 30, 4));
		localDeviceInited = true;
	}

}
