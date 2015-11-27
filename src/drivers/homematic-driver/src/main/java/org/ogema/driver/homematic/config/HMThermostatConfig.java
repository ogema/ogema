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
 * This class provides a list of configurations, that are supported by a type of
 * device. The list key is the unique name of the configuration. For each
 * physical instance of the device the list is cloned and fulfilled with the
 * data provided by the physical device.
 * 
 * @author mns
 *
 */
public class HMThermostatConfig extends HMDevConfI {
	// static final HashMap<String, HashMap<String, ListEntryValue>>
	// configsByDevice;
	static HashMap<String, ListEntryValue> supportedConfigs;

	// public boolean[] pendingRegisters;

	public HMThermostatConfig() {
		// pendingRegisters = new boolean[256];
		deviceConfigs = (HashMap<String, ListEntryValue>) supportedConfigs.clone();
	}

	static {
		// configsByDevice = new HashMap<String, HashMap<String,
		// ListEntryValue>>();

		supportedConfigs = new HashMap<String, ListEntryValue>(43);
		supportedConfigs.put("backOnTime", HMDevConfI.unInitedEntry);
		supportedConfigs.put("btnLock", HMDevConfI.unInitedEntry);
		supportedConfigs.put("btnNoBckLight", HMDevConfI.unInitedEntry);
		supportedConfigs.put("burstRx", HMDevConfI.unInitedEntry);
		supportedConfigs.put("cyclicInfoMsg", HMDevConfI.unInitedEntry);
		supportedConfigs.put("cyclicInfoMsgDis", HMDevConfI.unInitedEntry);
		supportedConfigs.put("globalBtnLock", HMDevConfI.unInitedEntry);
		supportedConfigs.put("localResDis", HMDevConfI.unInitedEntry);
		supportedConfigs.put("lowBatLimitRT", HMDevConfI.unInitedEntry);
		supportedConfigs.put("modusBtnLock", HMDevConfI.unInitedEntry);
		supportedConfigs.put("sign", HMDevConfI.unInitedEntry);
		supportedConfigs.put("dayTemp", HMDevConfI.unInitedEntry);
		supportedConfigs.put("nightTemp", HMDevConfI.unInitedEntry);
		supportedConfigs.put("tempMin", HMDevConfI.unInitedEntry);
		supportedConfigs.put("tempMax", HMDevConfI.unInitedEntry);
		supportedConfigs.put("tempOffset", HMDevConfI.unInitedEntry);
		supportedConfigs.put("decalcWeekday", HMDevConfI.unInitedEntry);
		supportedConfigs.put("decalcTime", HMDevConfI.unInitedEntry);
		supportedConfigs.put("boostPos", HMDevConfI.unInitedEntry);
		supportedConfigs.put("boostPeriod", HMDevConfI.unInitedEntry);
		supportedConfigs.put("daylightSaveTime", HMDevConfI.unInitedEntry);
		supportedConfigs.put("regAdaptive", HMDevConfI.unInitedEntry);
		supportedConfigs.put("showInfo", HMDevConfI.unInitedEntry);
		supportedConfigs.put("noMinMax4Manu", HMDevConfI.unInitedEntry);
		supportedConfigs.put("showWeekday", HMDevConfI.unInitedEntry);
		supportedConfigs.put("valveOffsetRt", HMDevConfI.unInitedEntry);
		supportedConfigs.put("valveMaxPos", HMDevConfI.unInitedEntry);
		supportedConfigs.put("valveErrPos", HMDevConfI.unInitedEntry);
		supportedConfigs.put("modePrioManu", HMDevConfI.unInitedEntry);
		supportedConfigs.put("modePrioParty", HMDevConfI.unInitedEntry);
		supportedConfigs.put("reguIntI", HMDevConfI.unInitedEntry);
		supportedConfigs.put("reguIntP", HMDevConfI.unInitedEntry);
		supportedConfigs.put("reguIntPstart", HMDevConfI.unInitedEntry);
		supportedConfigs.put("reguExtI", HMDevConfI.unInitedEntry);
		supportedConfigs.put("reguExtP", HMDevConfI.unInitedEntry);
		supportedConfigs.put("reguExtPstart", HMDevConfI.unInitedEntry);
		supportedConfigs.put("winOpnTemp", HMDevConfI.unInitedEntry);
		supportedConfigs.put("winOpnPeriod", HMDevConfI.unInitedEntry);
		supportedConfigs.put("winOpnBoost", HMDevConfI.unInitedEntry);
		supportedConfigs.put("winOpnMode", HMDevConfI.unInitedEntry);
		supportedConfigs.put("winOpnDetFall", HMDevConfI.unInitedEntry);
		supportedConfigs.put("CtrlRc", HMDevConfI.unInitedEntry);
		supportedConfigs.put("TempRC", HMDevConfI.unInitedEntry);
	}

	@Override
	public HashMap<String, ListEntryValue> getDevConfigs() {
		return deviceConfigs;
	}
}
