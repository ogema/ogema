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
