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
