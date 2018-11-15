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
import org.ogema.core.model.units.AngleResource;
import org.ogema.model.ranges.AngleRange;
import org.ogema.model.targetranges.AngleTargetRange;

/**
 * GenericFloatSensor for reactive power angle
 */
public interface ReactivePowerAngleSensor extends GenericFloatSensor {

	/**
	 * Measured phi (as in cos phi) for reactive power flow; positive for
	 * inductive loads.<br>
	 */
	@NonPersistent
	@Override
	AngleResource reading();

	/**
	 * Possible values for the angle.
	 *
	 * @see #reading()
	 */
	@Override
	AngleRange ratedValues();

	/**
	 * Setpoints for the sensor reading. Interpretation of the values is the
	 * same as in {@link #reading() }.
	 */
	@Override
	AngleTargetRange settings();

	/**
	 * Device setpoints for the sensor reading. Interpretation of the values is the
	 * same as in {@link #reading() }.
	 */
	@Override
	AngleTargetRange deviceSettings();

	/**
	 * Device setpoints' feedbacks for the sensor reading. Interpretation of the values is the
	 * same as in {@link #reading() }.
	 */
	@Override
	AngleTargetRange deviceFeedback();

}
