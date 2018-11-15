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
 * This class provides a list of devices, that are supported by configuration handling.
 * 
 * @author Zekeriya Mansuroglu
 *
 */
public class HMDeviceConfigs {
	static final HashMap<String, Class<?>> configsByType;

	// public static final HashMap<String, HMDevConfI> configsByAddr;
	static {
		configsByType = new HashMap<String, Class<?>>();

		configsByType.put("HM-CC-RT-DN", HMThermostatConfig.class);
		configsByType.put("HM-SEC-WDS-2", HMWaterDetectorConfig.class);
		configsByType.put("HM-ES-PMSw1-Pl", HMPowerMeterConfig.class);
	}

	public static HMDevConfI getDevConfig(String name) {
		Class<?> cls = configsByType.get(name);
		if (cls == null)
			cls = HMAllConfigs.class;
		HMDevConfI result = null;
		try {
			result = (HMDevConfI) cls.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return result;
	}
}
