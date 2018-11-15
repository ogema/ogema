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
package org.ogema.apps.sensorwarning.model;

import org.ogema.model.actors.OnOffSwitch;
import org.ogema.model.prototypes.Data;
import org.ogema.model.sensors.GenericFloatSensor;

/**
 * Configuration model for the CO2 warning application. Note that the alarm
 * limits are already part of the sensor's data model. This resource can be
 * put anywhere in the data model (probably as a top-level resource), since
 * sensor and actor are referenced.
 * 
 * @author Timo Fischer, Fraunhofer IWES
 */
public interface SensorWarningConfiguration extends Data {

	/**
	 * CO2 sensor to supervise (usually as a reference to the model.
	 */
	GenericFloatSensor sensor();

	/**
	 * Alarm switch to operate in case of a violation of the alarm limits.
	 */
	OnOffSwitch alarmSwitch();
}
