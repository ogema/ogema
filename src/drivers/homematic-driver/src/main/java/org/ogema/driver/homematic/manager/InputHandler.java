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
package org.ogema.driver.homematic.manager;

import java.util.Arrays;

import org.ogema.driver.homematic.tools.Converter;
import org.slf4j.Logger;

/**
 * Handles incoming messages and responds accordingly.
 * 
 * @author baerthbn
 * 
 */
public class InputHandler implements Runnable {
	public enum ResponseType {
		ACTIVE_ENDPOINT_RESPONSE, SIMPLE_DESCRIPTOR_RESPONSE, NODE_DESCRIPTOR_RESPONSE, IEEE_ADDR_RESPONSE, READ_ATTRIBUTES_RESPONSE, TRANSMIT_STATUS, WRITE_ATTRIBUTES_RESPONSE, REMOTE_NI_COMMAND
	};

	private volatile boolean running;
	private volatile Object inputEventLock;
	private volatile Object deviceHandlerLock;
	private MessageHandler messageHandler;
	private LocalDevice localDevice;

	private final Logger logger = org.slf4j.LoggerFactory.getLogger("homematic-driver");

	public InputHandler(LocalDevice localDevice) {
		inputEventLock = localDevice.getInputEventLock();
		deviceHandlerLock = localDevice.getDeviceHandlerLock();
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
						e1.printStackTrace();
					}
				}
				try {
					handleMessage(localDevice.getReceivedFrame());
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		}
	}

	private void handleMessage(byte[] tempArray) {
		switch (tempArray[0]) {
		case 'H':
			parseAdapterMsg(tempArray);
			break;
		case 'R':
		case 'E':
			StatusMessage emsg = new StatusMessage(tempArray);
			if (emsg.msg_type == 0x00 & localDevice.getPairing() != null) { // if pairing
				RemoteDevice temp_device = new RemoteDevice(localDevice, emsg);
				if (localDevice.getPairing().equals("0000000000")
						| localDevice.getPairing().equals(temp_device.getSerial())) {

					if ((localDevice.getDevices().get(temp_device.getAddress())) == null) {
						localDevice.getDevices().put(temp_device.getAddress(), temp_device);
						synchronized (deviceHandlerLock) {
							deviceHandlerLock.notify();
						}
					}
				}
			}
			else {
				if (localDevice.getDevices().containsKey(emsg.source)
						& (localDevice.getOwnerid().equals(emsg.destination) || emsg.destination.equals("000000"))) {
					// 000000 = broadcast
					logger.debug("InputHandler has device");
					messageHandler.messageReceived(emsg);
				}
			}
			break;
		case 'I':
			// This is needed for AES magic
			break;
		case 'G': // not yet seen
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
		localDevice.setOwnerid(new String(Converter.toHexString(data, 27, 3)));
		localDevice.setUptime((int) Converter.toLong(data, 30, 4));
	}

}
