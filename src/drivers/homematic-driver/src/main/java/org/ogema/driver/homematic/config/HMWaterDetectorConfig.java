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

public class HMWaterDetectorConfig extends HMDevConfI {
	static HashMap<String, ListEntryValue> supportedConfigs;

	HashMap<String, ListEntryValue> deviceConfigs;

	@SuppressWarnings("unchecked")
	public HMWaterDetectorConfig() {
		deviceConfigs = (HashMap<String, ListEntryValue>) supportedConfigs.clone();
	}

	static {
		supportedConfigs = new HashMap<String, ListEntryValue>(4);
		supportedConfigs.put("msgWdsPosA", HMDevConfI.unInitedEntry);
		supportedConfigs.put("msgWdsPosB", HMDevConfI.unInitedEntry);
		supportedConfigs.put("msgWdsPosC", HMDevConfI.unInitedEntry);
		supportedConfigs.put("eventFilterTimeB", HMDevConfI.unInitedEntry);
	}

	public HashMap<String, ListEntryValue> getDevConfigs() {
		return deviceConfigs;
	}
}
