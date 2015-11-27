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

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.driver.homematic.HMDriver;
import org.ogema.driver.homematic.config.HMDevConfI;
import org.ogema.driver.homematic.config.HMLookups;
import org.ogema.driver.homematic.config.ListEntry;
import org.ogema.driver.homematic.config.ListEntryValue;
import org.ogema.driver.homematic.manager.devices.CO2Detector;
import org.ogema.driver.homematic.manager.devices.MotionDetector;
import org.ogema.driver.homematic.manager.devices.PowerMeter;
import org.ogema.driver.homematic.manager.devices.Remote;
import org.ogema.driver.homematic.manager.devices.SmokeSensor;
import org.ogema.driver.homematic.manager.devices.THSensor;
import org.ogema.driver.homematic.manager.devices.Thermostat;
import org.ogema.driver.homematic.manager.devices.ThreeStateSensor;
import org.ogema.driver.homematic.manager.messages.CmdMessage;

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
	private final String name;

	private final SubDevice subDevice;

	// Message
	private String owner;
	private int nextMsgNum;
	public int sentMsgNum;

	// States
	private InitStates initState = InitStates.UNKNOWN;
	private LocalDevice localdevice;

	// Used from inputhandler for new devices
	public RemoteDevice(LocalDevice localdevice, StatusMessage msg) {
		this.address = msg.source;
		this.owner = localdevice.getOwnerid();
		this.type = msg.parseType();
		this.serial = msg.parseSerial();
		this.nextMsgNum = 1;
		this.localdevice = localdevice;
		this.subDevice = createSubDevice();
		this.name = localdevice.getDeviceDescriptor().getName(this.type);
	}

	// Used if device file is loading device
	public RemoteDevice(LocalDevice localdevice, String address, String type, String serial) {
		this.address = address;
		this.owner = localdevice.getOwnerid();
		this.type = type;
		this.serial = serial;
		this.nextMsgNum = 1;
		this.localdevice = localdevice;
		this.subDevice = createSubDevice();
		this.subDevice.addMandatoryChannels();
		setInitState(InitStates.PAIRED);
		this.name = localdevice.getDeviceDescriptor().getName(this.type);
		// createChannels();
	}

	public String getName() {
		return name;
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
		String s = localdevice.getDeviceDescriptor().getSubType(type);
		switch (s) {
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
			throw new RuntimeException("Type not supported: " + s);
		}
	}

	public void parseMsg(StatusMessage msg, CmdMessage cmd) {
		subDevice.parseMessage(msg, cmd);
		// this.msg_num = msg.msg_num + 1;
	}

	public void pushCommand(byte cmdFlag, byte cmdType, String data) {
		localdevice.sendCmdMessage(this, cmdFlag, cmdType, data);
	}

	public void pushConfig(String channel, String list, String configs) {
		localdevice.sendCmdMessage(this, (byte) 0xA0, (byte) 0x01, channel + "0500000000" + list);
		localdevice.sendCmdMessage(this, (byte) 0xA0, (byte) 0x01, channel + "08" + configs);
		localdevice.sendCmdMessage(this, (byte) 0xA0, (byte) 0x01, channel + "06");
	}

	public void pushConfig(int channel, int list, byte[] configs) {
		// see HACK in 10_CUL_HM.pm, for the Thermostat HM-CC-RT-DN the channel 0 is a shadow of channel 4
		if (name.equals("HM-CC-RT-DN")) {
			if (list == 7 && channel == 4)
				channel = 0;
		}
		byte[] arr1 = { (byte) channel, 0x05, 0x00, 0x00, 0x00, 0x00, (byte) list };
		localdevice.sendCmdMessage(this, (byte) 0xA0, (byte) 0x01, arr1); // init config

		int length = configs.length;
		byte[] arr2 = new byte[length + 2];
		arr2[0] = (byte) channel;
		arr2[1] = 0x08;
		int i = 0;
		while (length > 0) {
			arr2[i + 2] = configs[i];
			length--;
			i++;
		}
		localdevice.sendCmdMessage(this, (byte) 0xA0, (byte) 0x01, arr2); // send config
		byte[] arr3 = { (byte) channel, 0x06 };
		localdevice.sendCmdMessage(this, (byte) 0xA0, (byte) 0x01, arr3); // terminate config
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

	public int getMsgNum() {
		return (nextMsgNum = (nextMsgNum + 1) & 0x000000FF);
	}

	public void setMsgNum(int msg_num) {
		int next = msg_num & 0x000000FF;
		if (next > nextMsgNum)
			nextMsgNum = next;
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

	public void getAllConfigs() { // 00040000000000: chNUM[1]| |peer[4]|lst[1]
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
								System.out.println("Send Peer Conf on channel " + channel[2]);
								localdevice.sendCmdMessage(this, (byte) 0xA0, (byte) 0x01, "0" + channel[2] + "03");
								try {
									Thread.sleep(1500);
								} catch (InterruptedException e) {
								}

							}
						}
						else {
							System.out.println("Send device Conf on channel " + channel[2] + " and list " + lstPart[0]);
							localdevice.sendCmdMessage(this, (byte) 0xA0, (byte) 0x01, "0" + channel[2] + "04000000000"
									+ lstPart[0]);
							try {
								Thread.sleep(1500); // TODO calculate an appropriate time to wait until the request is
								// responded.
							} catch (InterruptedException e) {
							}
						}
					}
				}
			}
		}
	}

	public String readConfigValue(String configName) {
		HashMap<String, ListEntryValue> devconfigs = subDevice.configs.getDevConfigs();
		ListEntryValue entryV = devconfigs.get(configName);
		return entryV.getConfigValue(configName);
	}

	public String readConfigKey(String configName) {
		HashMap<String, ListEntryValue> devconfigs = subDevice.configs.getDevConfigs();
		ListEntryValue entryV = devconfigs.get(configName);
		return entryV.getConfigKey(configName);
	}

	public void removeSentMessage(CmdMessage cmd) {
		localdevice.getMessageHandler().sentCommands.remove(address + "" + cmd.sentNum);
	}

	// PUSH CONFIGURATION
	public void writeConfig(String confName, String setting) {
		HashMap<String, ListEntryValue> devconfigs = subDevice.configs.getDevConfigs();
		ListEntryValue entryV = devconfigs.get(confName);
		ListEntry entry = entryV.entry;
		int size = entry.size;
		int offset = entry.offsetBits;
		int pMask = ((1 << size) - 1);
		int nMask = ~(pMask << offset);
		int currentValue = entryV.getRegValue();
		String conv = entry.conversion;
		int regAddr = entry.register;

		int setValue = HMLookups.getValue2Setting(conv, setting, entry.factor);
		if (setValue == -1) {
			HMDriver.logger.error("Invalid setting %s", setting);
			return;
		}
		int bytesCnt = entry.getBytesCnt();
		byte[] configs = new byte[bytesCnt << 1];
		// write in big endian byte order
		setValue = setValue & pMask;
		setValue = setValue << offset;
		setValue = setValue | (currentValue & nMask);
		switch (bytesCnt) {
		case 1:
			configs[0] = (byte) (regAddr & 0xFF);
			configs[1] = (byte) (setValue & 0xFF);
			break;
		case 2:
			configs[0] = (byte) (regAddr & 0xFF);
			configs[1] = (byte) (setValue & 0xFF);
			configs[2] = (byte) (regAddr + 1 & 0xFF);
			configs[3] = (byte) (setValue >> 8 & 0xFF);
			break;
		case 3:
			configs[0] = (byte) (regAddr & 0xFF);
			configs[1] = (byte) (setValue & 0xFF);
			configs[2] = (byte) (regAddr + 1 & 0xFF);
			configs[3] = (byte) (setValue >> 8 & 0xFF);
			configs[4] = (byte) (regAddr + 2 & 0xFF);
			configs[5] = (byte) (setValue >> 16 & 0xFF);
			break;
		case 4:
			configs[0] = (byte) (regAddr & 0xFF);
			configs[1] = (byte) (setValue & 0xFF);
			configs[2] = (byte) (regAddr + 1 & 0xFF);
			configs[3] = (byte) (setValue >> 8 & 0xFF);
			configs[4] = (byte) (regAddr + 2 & 0xFF);
			configs[5] = (byte) (setValue >> 16 & 0xFF);
			configs[6] = (byte) (regAddr + 3 & 0xFF);
			configs[7] = (byte) (setValue >> 24 & 0xFF);
			break;
		default:
			break;
		}
		pushConfig(entry.channel, entry.list, configs);
	}

	public String listSupportedConfigs() {
		StringBuffer result = new StringBuffer();
		HMDevConfI devconfig = subDevice.configs;
		if (devconfig == null) {
			result.append(this.toString());
			result.append(" is not yet supported by configuration handling.");
		}
		else {
			HashMap<String, ListEntryValue> configs = devconfig.getDevConfigs();
			Set<Entry<String, ListEntryValue>> set = configs.entrySet();
			for (Entry<String, ListEntryValue> entry : set) {
				result.append(entry.getKey());
				result.append('\t');
				ListEntryValue entryVal = entry.getValue();
				if (entryVal == null)
					result.append(" : unexpectedly no value is provided by the device.");
				else
					result.append(entry.getValue().getDescription());
				result.append('\n');
			}
		}
		return result.toString();
	}
}
