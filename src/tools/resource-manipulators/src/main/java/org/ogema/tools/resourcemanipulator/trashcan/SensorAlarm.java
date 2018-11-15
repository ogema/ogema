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
