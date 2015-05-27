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
package org.ogema.driver.homematic.manager;

import org.ogema.driver.homematic.manager.devices.CO2Detector;
import org.ogema.driver.homematic.manager.devices.MotionDetector;
import org.ogema.driver.homematic.manager.devices.PowerMeter;
import org.ogema.driver.homematic.manager.devices.Remote;
import org.ogema.driver.homematic.manager.devices.SmokeSensor;
import org.ogema.driver.homematic.manager.devices.THSensor;
import org.ogema.driver.homematic.manager.devices.Thermostat;
import org.ogema.driver.homematic.manager.devices.ThreeStateSensor;

/**
 * Represents a remote endpoint in a Homematic network.
 * 
 * @author baerthbn
 * 
 */
public class RemoteDevice {

	// TODO: AES-Key !
	public enum InitStates {
		UNKNOWN, PAIRING, PAIRED
	}

	private final String address;
	private final String type;
	private final String serial;

	private final SubDevice subDevice;

	// Message
	private String owner;
	private long msg_num;

	// States
	private InitStates initState = InitStates.UNKNOWN;
	private LocalDevice localdevice;

	// Used from inputhandler for new devices
	public RemoteDevice(LocalDevice localdevice, StatusMessage msg) {
		this.address = msg.source;
		this.owner = localdevice.getOwnerid();
		this.type = msg.parseType();
		this.serial = msg.parseSerial();
		this.msg_num = 1;
		this.localdevice = localdevice;
		this.subDevice = createSubDevice();
	}

	// Used if device file is loading device
	public RemoteDevice(LocalDevice localdevice, String address, String type, String serial) {
		this.address = address;
		this.owner = localdevice.getOwnerid();
		this.type = type;
		this.serial = serial;
		this.msg_num = 1;
		this.localdevice = localdevice;
		this.subDevice = createSubDevice();
		this.subDevice.addMandatoryChannels();
		setInitState(InitStates.PAIRED);
		// createChannels();
	}

	public void init() {
		String configs = "0201";
		configs += "0A" + owner.charAt(0) + owner.charAt(1) + "0B" + owner.charAt(2) + owner.charAt(3) + "0C"
				+ owner.charAt(4) + owner.charAt(5);
		pushConfig("00", "00", configs);
		setInitState(InitStates.PAIRING);
		// AES aktivieren
		// pushConfig("01", "01", "0801");
		this.subDevice.addMandatoryChannels();
	}

	private SubDevice createSubDevice() {
		switch (localdevice.getDeviceDescriptor().getSubType(type)) {
		case "THSensor":
			return new THSensor(this);
		case "threeStateSensor":
			return new ThreeStateSensor(this);
		case "thermostat":
			return new Thermostat(this);
		case "powerMeter":
			return new PowerMeter(this);
		case "smokeDetector":
			return new SmokeSensor(this);
		case "CO2Detector":
			return new CO2Detector(this);
		case "motionDetector":
			return new MotionDetector(this);
		case "remote":
		case "pushbutton":
		case "swi":
			return new Remote(this);
		default:
			throw new RuntimeException("Type not supported");
		}
	}

	public void parseMsg(StatusMessage msg) {
		subDevice.parseValue(msg);
		this.msg_num = msg.msg_num + 1;
	}

	public void pushCommand(byte cmdFlag, byte cmdType, String data) {
		localdevice.sendCmdMessage(this, cmdFlag, cmdType, data);
	}

	public void pushConfig(String channel, String list, String configs) {
		localdevice.sendCmdMessage(this, (byte) 0xA0, (byte) 0x01, channel + "0500000000" + list);
		localdevice.sendCmdMessage(this, (byte) 0xA0, (byte) 0x01, channel + "08" + configs);
		localdevice.sendCmdMessage(this, (byte) 0xA0, (byte) 0x01, channel + "06");
	}

	public String getAddress() {
		return address;
	}

	public String getDeviceType() {
		return type;
	}

	public String getSerial() {
		return serial;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public long getMsg_num() {
		return msg_num;
	}

	public void setMsg_num(long msg_num) {
		this.msg_num = msg_num;
	}

	public SubDevice getSubDevice() {
		return this.subDevice;
	}

	public InitStates getInitState() {
		return initState;
	}

	public void setInitState(InitStates initState) {
		this.initState = initState;
	}

	public String[] getChannels() {
		return localdevice.getDeviceDescriptor().getChannels(type);
	}

	public void getConfig() { // 00040000000000: chNUM[1]| |peer[4]|lst[1]
		localdevice.sendCmdMessage(this, (byte) 0xA0, (byte) 0x01, "00040000000000"); // TODO: last byte should be
		// listnumber, first byte the channel number!
		String[] channels = localdevice.getDeviceDescriptor().getChannels(type);
		String[] lists = localdevice.getDeviceDescriptor().getLists(type);
		if (channels.length == 1)
			channels[0] = "autocreate:1:1";
		for (String chnstr : channels) {
			String[] channel = chnstr.split(":");
			if (lists.length != 0) {
				boolean pReq = false;
				for (String listEntry : lists) {
					boolean peerReq = false;
					boolean chnValid = false;
					String[] lstPart = listEntry.split(":");
					if (lstPart.length == 1) {
						chnValid = true;
						if (lstPart[0].equals("p") || lstPart[0].equals("3") || lstPart[0].equals("4"))
							peerReq = true;
					}
					else {
						String test = new String(lstPart[1]);
						String[] chnLst = test.split("\\.");
						for (String lchn : chnLst) {
							if (lchn.contains(channel[2]))
								chnValid = true;
							if (chnValid && lchn.contains("p"))
								peerReq = true;
						}
					}

					if (chnValid) {
						if (peerReq) {
							if (!pReq) {
								pReq = true;
								localdevice.sendCmdMessage(this, (byte) 0xA0, (byte) 0x01, "0" + channel[2] + "03");
							}
						}
						else {
							localdevice.sendCmdMessage(this, (byte) 0xA0, (byte) 0x01, "0" + channel[2] + "04000000000"
									+ lstPart[0]);
						}
					}
				}
			}
		}
	}
}
