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
