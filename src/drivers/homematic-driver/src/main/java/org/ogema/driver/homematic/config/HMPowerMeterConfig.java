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
