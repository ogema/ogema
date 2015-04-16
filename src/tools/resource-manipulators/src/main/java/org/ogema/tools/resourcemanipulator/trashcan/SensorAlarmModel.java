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
import org.ogema.tools.resourcemanipulator.model.ResourceManipulatorModel;
import org.ogema.tools.resourcemanipulator.trashcan.SensorAlarm;

/**
 * Configuration model for the {@link SensorAlarm}. Note that the alarm
 * limits are already part of the sensor's data model. 
 * 
 * @author Timo Fischer, Fraunhofer IWES
 */
public interface SensorAlarmModel extends ResourceManipulatorModel {

	/**
	 * Sensor to supervise (usually as a reference to the model.
	 */
	GenericFloatSensor sensor();

	/**
	 * Alarm switch to operate in case of a violation of the alarm limits.
	 */
	OnOffSwitch alarmSwitch();
}
