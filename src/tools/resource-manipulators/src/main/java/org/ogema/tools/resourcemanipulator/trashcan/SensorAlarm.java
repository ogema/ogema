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
package org.ogema.tools.resourcemanipulator.trashcan;

import org.ogema.model.actors.OnOffSwitch;
import org.ogema.model.sensors.GenericFloatSensor;
import org.ogema.model.sensors.Sensor;
import org.ogema.tools.resourcemanipulator.ResourceManipulator;

/**
 * Configures a pair of a {@link Sensor} and an {@link OnOffSwitch} such that
 * the switch is triggered in case the sensor reading violates the alarm limits.
 */
public interface SensorAlarm extends ResourceManipulator {

	/**
	 * Adds a new sensor, actor pair to the list of automatic sensor-alarmings.
	 * @param sensor Sensor that shall be supervised.
	 * @param onOffSwitch Switch that shall be triggered when the sensor alarm limits are being violated.
	 */
	void add(GenericFloatSensor sensor, OnOffSwitch onOffSwitch);

	/**
	 * Removes a sensor, actor pair from the list of 
	 */
}
