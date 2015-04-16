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
package org.ogema.model.sensors;

import org.ogema.core.model.ModelModifiers.NonPersistent;
import org.ogema.core.model.SimpleResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.model.prototypes.PhysicalElement;
import org.ogema.model.ranges.Range;
import org.ogema.model.targetranges.TargetRange;

/**
 * Prototype to be extended by resources representing a sensor or a measured
 * property. The prototype is usually not created as a
 * resource itself, but is the basis for different data models for different
 * physical quantities to be measured. For sensors measuring a true/false
 * quantity, use {@link BinarySensor} as a basis model, instead.
 */
public interface Sensor extends PhysicalElement {

	/**
	 * The sensor reading. Models inheriting from this prototype must override
	 * this with a suitable simple resource to define the meaning and the unit
	 * of measurement.
	 */
	@NonPersistent
	SimpleResource reading();

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
