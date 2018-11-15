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
package org.ogema.model.sensors;

import org.ogema.core.model.ModelModifiers.NonPersistent;
import org.ogema.core.model.ValueResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.model.devices.storage.ElectricityStorage;
import org.ogema.model.prototypes.PhysicalElement;
import org.ogema.model.ranges.Range;
import org.ogema.model.targetranges.TargetRange;

/**
 * Prototype to be extended by resources representing a sensor or a measured
 * property. The prototype is usually not created as a
 * resource itself, but is the basis for different data models for different
 * physical quantities to be measured. For sensors measuring a true/false
 * quantity, use BinarySensor as a basis model, instead.
 */
public interface Sensor extends PhysicalElement {

	/**
	 * The sensor reading. Models inheriting from this prototype must override
	 * this with a suitable simple resource to define the meaning and the unit
	 * of measurement.
	 */
	@NonPersistent
	ValueResource reading();

	/**
	 * Rated value for the physical property defined by the sensor. <br>
	 * Models inheriting from this prototype must override this with a suitable
	 * range resource to define the unit of measurement. Meaning and unit of the
	 * range entries must be identical to {@link #reading() }.
	 */
	Range ratedValues();

	/**
	 * Control and alarm settings for the value that the management software should
	 * take into account. For the settings sent to the device, use {@link #deviceSettings() }. 
	 * Must be overwritten by a more
	 * specialized TargetRange type in real sensors.
	 */
	TargetRange settings();

	/**
	 * GenericFloatSensor-related settings to be sent to external device for
	 * configuration of communication and alarm handling. Sensors representing
	 * only a measurement but not an actual sensor device do not use this field.
	 * Conversely, real sensors represented can create and activate this field
	 * to indicate that there is an actual device involved. This can be done
	 * even if no further entries are added below this field. 
	 * The feedbacks
	 * of these settings are in {@link #deviceFeedback() }.
	 */
	TargetRange deviceSettings();

	/**
	 * Feedback values for the alarm settings as they are reported back by
	 * the device. If the sensor only represents a measurement but there is
	 * no actual device performing the measurement, this is not used.
	 * @see #deviceSettings() 
	 */
	TargetRange deviceFeedback();
	
	/**
	 * The battery status is available in the subresource
	 * battery/chargeSensor/reading.
	 */
	ElectricityStorage battery();

	/**
	 * Time of measurement (for real sensors) or time of last update (for sensor
	 * readings not associated with a real measurement device).
	 */
	@NonPersistent
	public TimeResource readingTimestamp();

	/**
	 * Measurement quality type<br>
	 * 0: actual measurement or value okay - the reading of the sensor
	 * represents what is expected<br>
	 * 1: substitute value. The reading is not what is expected but a good
	 * substitute could be calculated<br>
	 * 2: default value
	 */
	@NonPersistent
	public IntegerResource readingType();

	/**
	 * Root-mean-square readingError of measurement values. This value is
	 * provided as a general configuration information and relates to the total
	 * set of measurements, i.e. it is not a property of an individual measured
	 * value. Note that technically, this can also be defined for binary
	 * sensors.<br>
	 * Unit: Same as the sensor measurement. Should explicitly be overwritten in
	 * devices extending this type, where possible.
	 */
	public FloatResource readingError();

}
