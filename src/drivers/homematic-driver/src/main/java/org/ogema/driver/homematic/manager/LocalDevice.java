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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.ogema.driver.homematic.Activator;
import org.ogema.driver.homematic.manager.messages.CmdMessage;
import org.ogema.driver.homematic.tools.DeviceDescriptor;
import org.ogema.driver.homematic.usbconnection.IUsbConnection;
import org.ogema.driver.homematic.usbconnection.UsbConnection;

public class LocalDevice {
	private final Map<String, RemoteDevice> devices;
	private IUsbConnection connection;
	private InputHandler inputHandler;
	private Thread inputHandlerThread;
	private MessageHandler messageHandler;
	private FileStorage fileStorage;
	private final DeviceDescriptor deviceDescriptor;

	private String pairing = null; // null = nothing; 0000000000 = pair all; XXXXXXXXXX = pair serial

	private String name = "N/A";
	private String ownerid = "000000";
	private String serial = "0000000000";
	private String firmware = "0.0";
	private int uptime = 0;
	private volatile boolean isReady = false;

	public LocalDevice(String port, UsbConnection con) {
		connection = con;
		devices = new ConcurrentHashMap<String, RemoteDevice>();
		deviceDescriptor = new DeviceDescriptor();

		messageHandler = new MessageHandler(this);

		inputHandler = new InputHandler(this);
		inputHandlerThread = new Thread(inputHandler);
		inputHandlerThread.setName("homematic-lld-inputHandler");
		inputHandlerThread.start();
		final LocalDevice loc = this;

		Thread loadDevicesThread = new Thread() {
			@Override
			public void run() {
				while (!isReady && Activator.bundleIsRunning) {
					try {
						Thread.sleep(500);
					} catch (InterruptedException ex) {
						ex.printStackTrace();
					}
				}
				fileStorage = new FileStorage(loc);
			}
		};
		loadDevicesThread.setName("homematic-ll-loadDevices");
		loadDevicesThread.start();
	}

	public void closeUsbConnection() {
		connection.closeConnection();
	}

	public Object getInputEventLock() {
		return connection.getInputEventLock();
	}

	public Map<String, RemoteDevice> getDevices() {
		return devices;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOwnerid() {
		return ownerid;
	}

	public void setOwnerid(String ownerid) {
		this.ownerid = ownerid;
		connection.setConnectionAddress(ownerid);
		this.isReady = true;
	}

	public String getSerial() {
		return serial;
	}

	public void setSerial(String serial) {
		this.serial = serial;
	}

	public String getFirmware() {
		return firmware;
	}

	public void setFirmware(String firmware) {
		this.firmware = firmware;
	}

	public int getUptime() {
		return uptime;
	}

	public void setUptime(int uptime) {
		this.uptime = uptime;
	}

	public void sendCmdMessage(RemoteDevice rd, byte flag, byte type, String data) {
		CmdMessage cmdMessage = new CmdMessage(this, rd, flag, type, data);
		messageHandler.sendMessage(cmdMessage);
	}

	public void sendCmdMessage(RemoteDevice rd, byte flag, byte type, byte[] data) {
		CmdMessage cmdMessage = new CmdMessage(this, rd, flag, type, data);
		messageHandler.sendMessage(cmdMessage);
	}

	public MessageHandler getMessageHandler() {
		return messageHandler;
	}

	public boolean connectionHasFrames() {
		return connection.hasFrames();
	}

	public byte[] getReceivedFrame() {
		return connection.getReceivedFrame();
	}

	public void sendFrame(byte[] frame) {
		connection.sendFrame(frame);
	}

	public IUsbConnection getConnection() {
		return connection;
	}

	public FileStorage getFileStorage() {
		return fileStorage;
	}

	public DeviceDescriptor getDeviceDescriptor() {
		return deviceDescriptor;
	}

	/**
	 * enable pairing with a specific device. 
	 * @param val serial number of the device as 5 Bytes Hex 
	 */
	public void setPairing(String val) {
		this.pairing = val;
	}

	public String getPairing() {
		return this.pairing;
	}

	public void close() {

		closeUsbConnection();
		inputHandler.stop();
		inputHandlerThread.interrupt();
	}

	public void saveDeviceConfig() {
		if (isReady)
			fileStorage.saveDeviceConfig();
	}

	public void isReady(boolean val) {
		isReady = val;
	}

	public void restart() {

	}
}
