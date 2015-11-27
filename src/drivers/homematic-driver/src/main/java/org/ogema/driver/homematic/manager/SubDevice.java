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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.driver.homematic.config.HMDevConfI;
import org.ogema.driver.homematic.config.HMDeviceConfigs;
import org.ogema.driver.homematic.config.HMList;
import org.ogema.driver.homematic.config.ListEntry;
import org.ogema.driver.homematic.config.ListEntryValue;
import org.ogema.driver.homematic.manager.messages.CmdMessage;

public abstract class SubDevice {

	// private final Logger logger = org.slf4j.LoggerFactory.getLogger("homematic-driver");

	private static final byte CONFIG_RESPONSE_TYPE1 = (byte) 0x02;
	private static final byte CONFIG_RESPONSE_TYPE2 = (byte) 0x03;

	protected RemoteDevice remoteDevice;

	public Map<Byte, DeviceCommand> deviceCommands;
	public Map<Short, DeviceAttribute> deviceAttributes;

	HMDevConfI configs;

	public SubDevice(RemoteDevice rd) {
		this.remoteDevice = rd;

		deviceCommands = new HashMap<Byte, DeviceCommand>();
		deviceAttributes = new HashMap<Short, DeviceAttribute>();
	}

	protected abstract void addMandatoryChannels();

	public abstract void parseValue(StatusMessage msg);

	public abstract void parseMessage(StatusMessage msg, CmdMessage cmd);

	public abstract void channelChanged(byte identifier, Value value);

	// READ CONFIGURATIONS
	public void parseConfig(StatusMessage response, CmdMessage cmd) {
		if (configs == null)
			configs = HMDeviceConfigs.getDevConfig(remoteDevice.getName());

		if (cmd == null)
			return; // should never happen
		// update message number for the case that the remote device has incremented it.
		remoteDevice.setMsgNum(response.msg_num);
		byte contentType = response.msg_data[0];
		// int length = (int) response.msg_data.length - 1;
		// int offset = 1;
		// get the relevant request data, list, channel and peer
		boolean peer = cmd.data[1] == 3; // 1. byte is 03 for peer configuration or 04 own configuration
		if (peer) {
			System.out.println("Peer Configurations: ignored");
			return;
		}
		else
			System.out.println("Device Configurations: ");
		int list;
		if (peer)
			list = 0;
		else
			list = cmd.data[6];
		int channel = cmd.data[0];
		// HMList listObj = HMList.allLists[list];

		switch (contentType) {
		case CONFIG_RESPONSE_TYPE1:
			getRegisterValues1(response.msg_data, list);
			break;
		case CONFIG_RESPONSE_TYPE2:
			getRegisterValues2(response.msg_data, list);
			break;
		default:
			break;
		}
		parseRegisterEntry(list, channel);
		// TODO remoteDevice.removeSentMessage(cmd); // Garbage collection ist etwas aufwendiger, fehlende Information:
		// wann ist die Antwort auf ein bestimmtes Commando abgeschlossen? Deswegen ist ein immer wieder kehrender
		// Prozess erforderlich, der die alten CommandMessages entfernt.
	}

	private void getRegisterValues1(byte[] msg_data, int list) {
		Map<Integer, Integer> target = configs.getRegValues(list);
		int length = (int) msg_data.length - 1;
		int offset = 1;
		while (length > 1) { // >1 because to each value belong two bytes
			int register = msg_data[offset++];
			int value = msg_data[offset++];
			target.put(register, value);
			length -= 2;
		}
	}

	private void getRegisterValues2(byte[] msg_data, int list) {
		int length = (int) msg_data.length - 1;
		int offset = 1;
		int register = msg_data[offset++];
		length--;
		Map<Integer, Integer> target = configs.getRegValues(list);
		while (length > 0) { // > 0 because the start register is followed by a values each byte
			int value = msg_data[offset++];
			target.put(register++, value);
			length--;
		}
	}

	void parseRegisterEntry(int list, int channel) {
		HMList listObj = HMList.allLists[list];
		Map<Integer, Integer> registers = configs.getRegValues(list);
		// boolean pending;
		Set<Entry<Integer, Integer>> set = registers.entrySet();
		for (Entry<Integer, Integer> regEntry : set) {
			int register = regEntry.getKey();
			Object o = listObj.entriesByRegs.get(register);
			List<ListEntryValue> valueList = getMatchingEntry(o);
			for (ListEntryValue entryVal : valueList) {
				if (entryVal == null)
					System.out.println("Not found reg: " + register + " in list: " + list);
				else {
					System.out.println("Found reg: " + register + " in list: " + list);
					// int bytes = parseValue(entryVal, registers, register);
					entryVal.entry.channel = channel;
					System.out.println(entryVal.getDescription());
				}
			}
		}
	}

	private List<ListEntryValue> getMatchingEntry(Object o) {
		ArrayList<ListEntryValue> result = new ArrayList<ListEntryValue>();
		if (o == null)
			return result;
		// String addr = remoteDevice.getAddress();
		// HashMap<String, ListEntryValue> devoptions = null;
		// devoptions = configs.getDevConfigs();
		if (!(o instanceof ListEntry)) {
			@SuppressWarnings("unchecked")
			ArrayList<ListEntry> list = (ArrayList<ListEntry>) o;
			for (ListEntry e : list) {
				ListEntryValue entryVal = configs.getEntryValue(e);// getListEntryValue(e, devoptions);
				if (entryVal != null) {
					result.add(entryVal);
				}
			}
		}
		else {
			ListEntryValue entryVal = configs.getEntryValue(((ListEntry) o));// getListEntryValue((ListEntry) o, //
			// devoptions);
			if (entryVal != null) {
				result.add(entryVal);
			}
		}
		return result;
	}

	// ListEntryValue getListEntryValue(ListEntry e, Map<String, ListEntryValue> devconfigs) {
	// ListEntryValue result = null;
	// String tmpName = e.name;
	// if (devconfigs != null) {
	// result = devconfigs.get(tmpName);
	// if (result == null) {
	// result = new ListEntryValue(e, -1);
	// devconfigs.put(tmpName, result);
	// }
	// else {
	// logger.debug("Configuration " + tmpName + " reported by the device is not supported by the driver.");
	// }
	// }
	// return result;
	// }

	// private int parseValue(ListEntryValue o, Map<Integer, Integer> regs, int register) {
	// int result = 0;
	// int value = 0;
	// int byteCount = o.entry.getBytesCnt();
	// result = byteCount;
	// while (byteCount > 0) {
	// value <<= 8;
	// value |= (0x000000FF) & regs.get(register++);
	// byteCount--;
	// }
	// ((ListEntryValue) o).setRegValue(value);
	// return result;
	// }

}
