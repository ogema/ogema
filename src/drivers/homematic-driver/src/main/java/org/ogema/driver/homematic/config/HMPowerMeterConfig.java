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
package org.ogema.driver.homematic.config;

import java.util.HashMap;

/**
 * This class provides a list of configurations, that are supported by a type of device. The list key is the unique name
 * of the configuration. For each physical instance of the device the list is cloned and fulfilled with the data
 * provided by the physical device.
 * 
 * @author mns
 *
 */
public class HMPowerMeterConfig extends HMDevConfI {
	// static final HashMap<String, HashMap<String, ListEntryValue>>
	// configsByDevice;
	static HashMap<String, ListEntryValue> supportedConfigs;

	// public boolean[] pendingRegisters;

	public HMPowerMeterConfig() {
		// pendingRegisters = new boolean[256];
		deviceConfigs = (HashMap<String, ListEntryValue>) supportedConfigs.clone();
	}

	static {
		// configsByDevice = new HashMap<String, HashMap<String,
		// ListEntryValue>>();

		supportedConfigs = new HashMap<String, ListEntryValue>(43);
		supportedConfigs.put("ActionType", HMDevConfI.unInitedEntry);
		supportedConfigs.put("averaging", HMDevConfI.unInitedEntry);
		supportedConfigs.put("cndTxCycAbove", HMDevConfI.unInitedEntry);
		supportedConfigs.put("cndTxCycBelow", HMDevConfI.unInitedEntry);
		supportedConfigs.put("cndTxDecAbove", HMDevConfI.unInitedEntry);
		supportedConfigs.put("cndTxDecBelow", HMDevConfI.unInitedEntry);
		supportedConfigs.put("cndTxFalling", HMDevConfI.unInitedEntry);
		supportedConfigs.put("cndTxRising", HMDevConfI.unInitedEntry);
		supportedConfigs.put("CtDlyOff", HMDevConfI.unInitedEntry);
		supportedConfigs.put("CtDlyOn", HMDevConfI.unInitedEntry);
		supportedConfigs.put("CtOff", HMDevConfI.unInitedEntry);
		supportedConfigs.put("CtOn", HMDevConfI.unInitedEntry);
		supportedConfigs.put("CtValHi", HMDevConfI.unInitedEntry);
		supportedConfigs.put("CtValLo", HMDevConfI.unInitedEntry);
		supportedConfigs.put("expectAES", HMDevConfI.unInitedEntry);
		supportedConfigs.put("ledOnTime", HMDevConfI.unInitedEntry);
		supportedConfigs.put("lgMultiExec", HMDevConfI.unInitedEntry);
		supportedConfigs.put("OffDly", HMDevConfI.unInitedEntry);
		supportedConfigs.put("OffTime", HMDevConfI.unInitedEntry);
		supportedConfigs.put("OffTimeMode", HMDevConfI.unInitedEntry);
		supportedConfigs.put("OnDly", HMDevConfI.unInitedEntry);
		supportedConfigs.put("OnTime", HMDevConfI.unInitedEntry);
		supportedConfigs.put("OnTimeMode", HMDevConfI.unInitedEntry);
		supportedConfigs.put("peerNeedsBurst", HMDevConfI.unInitedEntry);
		supportedConfigs.put("powerUpAction", HMDevConfI.unInitedEntry);
		supportedConfigs.put("sign", HMDevConfI.unInitedEntry);
		supportedConfigs.put("statusInfoMinDly", HMDevConfI.unInitedEntry);
		supportedConfigs.put("statusInfoRandom", HMDevConfI.unInitedEntry);
		supportedConfigs.put("SwJtDlyOff", HMDevConfI.unInitedEntry);
		supportedConfigs.put("SwJtDlyOn", HMDevConfI.unInitedEntry);
		supportedConfigs.put("SwJtOff", HMDevConfI.unInitedEntry);
		supportedConfigs.put("SwJtOn", HMDevConfI.unInitedEntry);
		supportedConfigs.put("transmitTryMax", HMDevConfI.unInitedEntry);
		supportedConfigs.put("txMinDly", HMDevConfI.unInitedEntry);
		supportedConfigs.put("txThrCur", HMDevConfI.unInitedEntry);
		supportedConfigs.put("txThrFrq", HMDevConfI.unInitedEntry);
		supportedConfigs.put("txThrHiCur", HMDevConfI.unInitedEntry);
		supportedConfigs.put("txThrHiFrq", HMDevConfI.unInitedEntry);
		supportedConfigs.put("txThrHiPwr", HMDevConfI.unInitedEntry);
		supportedConfigs.put("txThrHiVlt", HMDevConfI.unInitedEntry);
		supportedConfigs.put("txThrLoCur", HMDevConfI.unInitedEntry);
		supportedConfigs.put("txThrLoFrq", HMDevConfI.unInitedEntry);
		supportedConfigs.put("txThrLoPwr", HMDevConfI.unInitedEntry);
		supportedConfigs.put("txThrLoVlt", HMDevConfI.unInitedEntry);
		supportedConfigs.put("txThrPwr", HMDevConfI.unInitedEntry);
		supportedConfigs.put("txThrVlt", HMDevConfI.unInitedEntry);
	}

	@Override
	public HashMap<String, ListEntryValue> getDevConfigs() {
		return deviceConfigs;
	}
}
