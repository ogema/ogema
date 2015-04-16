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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.ogema.core.channelmanager.measurements.ByteArrayValue;
import org.ogema.core.channelmanager.measurements.LongValue;
import org.ogema.driver.homematic.tools.Converter;
import org.slf4j.Logger;

/**
 * Represents a remote endpoint in a Homematic network.
 * 
 * @author baerthbn
 * 
 */
public class RemoteDevice {

	private final Logger logger = org.slf4j.LoggerFactory.getLogger("homematic-driver");

	// TODO: AES-Key !
	public enum InitStates {
		UNKNOWN, PAIRING, PAIRED
	}

	private final String address;
	private final String type;
	private final String serial;

	// Message
	private String owner;
	private long msg_num;

	// States
	private InitStates initState = InitStates.UNKNOWN;
	private LocalDevice localdevice;

	// ChannelManagerImpl
	public Map<Byte, DeviceCommand> deviceCommands = new HashMap<Byte, DeviceCommand>();
	public Map<Short, DeviceAttribute> deviceAttributes = new HashMap<Short, DeviceAttribute>();

	public RemoteDevice(LocalDevice localdevice, StatusMessage msg) {
		this.address = msg.source;
		this.owner = localdevice.getOwnerid();
		this.type = new String(Converter.toHexString(msg.msg_data, 1, 2));
		this.serial = new String(Arrays.copyOfRange(msg.msg_data, 3, 13));
		this.msg_num = 1;

		this.localdevice = localdevice;
	}

	public RemoteDevice(LocalDevice localdevice, String address, String type, String serial) {
		this.address = address;
		this.owner = localdevice.getOwnerid();
		this.type = type;
		this.serial = serial;
		this.msg_num = 1;

		this.localdevice = localdevice;
		setInitState(InitStates.PAIRED);
		createChannels();
	}

	public void init() {
		String configs = "0201";
		configs += "0A" + owner.charAt(0) + owner.charAt(1) + "0B" + owner.charAt(2) + owner.charAt(3) + "0C"
				+ owner.charAt(4) + owner.charAt(5);
		pushConfig("00", "00", configs);
		setInitState(InitStates.PAIRING);
		// AES aktivieren
		// pushConfig("01", "01", "0801");
		createChannels();
	}

	private void createChannels() {
		deviceAttributes.put((short) 0x0001, new DeviceAttribute((short) 0x0001, "MessagePayload", true, true));
		deviceAttributes.put((short) 0x0002, new DeviceAttribute((short) 0x0002, "Uptime", true, true));
		deviceAttributes.put((short) 0x0003, new DeviceAttribute((short) 0x0003, "RSSI", true, true));
		deviceAttributes.put((short) 0x0004, new DeviceAttribute((short) 0x0004, "Status", true, true));
		deviceAttributes.put((short) 0x0005, new DeviceAttribute((short) 0x0005, "Condition", true, true));

		deviceCommands.put((byte) 0x01, new DeviceCommand(this, (byte) 0x01, "StandardCommand", true));
		deviceCommands.put((byte) 0x02, new DeviceCommand(this, (byte) 0x02, "Config", true));
	}

	public void parseMsg(StatusMessage msg) {
		this.deviceAttributes.get((short) 0x0002).setValue(new LongValue(msg.uptime));
		this.deviceAttributes.get((short) 0x0003).setValue(new LongValue(msg.rssi));
		this.deviceAttributes.get((short) 0x0004).setValue(new LongValue(msg.status));
		this.deviceAttributes.get((short) 0x0005).setValue(new LongValue(msg.cond));
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

	public void setValue(byte[] data, byte msgflag, byte msgtype) {
		byte[] value = ArrayUtils.add(data, msgflag);
		value = ArrayUtils.add(value, msgtype);
		this.deviceAttributes.get((short) 0x0001).setValue(new ByteArrayValue(value));
	}

	public void performCommand(byte identifier, byte[] data) {
		byte[] msg = null;
		switch (identifier) {
		case 0x01:
			byte msgFlag = data[data.length - 2];
			byte msgType = data[data.length - 1];
			msg = ArrayUtils.removeAll(data, data.length - 2, data.length - 1);
			pushCommand(msgFlag, msgType, Converter.toHexString(msg));
			break;
		case 0x02:
			String chn = Converter.toHexString(data[data.length - 2]);
			String lst = Converter.toHexString(data[data.length - 1]);
			msg = ArrayUtils.removeAll(data, data.length - 2, data.length - 1);
			pushConfig(chn, lst, Converter.toHexString(msg));
			break;
		default:
			logger.error("Command Channel Identifier Error");
		}
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

	public InitStates getInitState() {
		return initState;
	}

	public void setInitState(InitStates initState) {
		this.initState = initState;
	}
}
