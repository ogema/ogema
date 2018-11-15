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
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.units.AngleResource;
import org.ogema.core.model.units.EnergyPerAreaResource;
import org.ogema.model.ranges.EnergyPerAreaRange;
import org.ogema.model.targetranges.EnergyPerAreaTargetRange;

/**
 * GenericFloatSensor measuring the solar energy irradiated on the target area.
 */
public interface SolarIrradiationSensor extends GenericFloatSensor {

	/**
	 * Measured solar radiation flux density
	 */
	@NonPersistent
	@Override
	EnergyPerAreaResource reading();

	/**
	 * Rated values for the reading.
	 */
	@Override
	EnergyPerAreaRange ratedValues();

	/**
	 * Settings for the reading.
	 */
	@Override
	EnergyPerAreaTargetRange settings();

	@Override
	EnergyPerAreaTargetRange deviceSettings();

	@Override
	EnergyPerAreaTargetRange deviceFeedback();

	/**
	 * Angle of inclination of a module directing to south the forecast is made
	 * for<br>
	 * unit: ° (angle degree; horizontal (flat):0°, vertical:90°)
	 */
	AngleResource inclination();

	/**
	 * Type of temperature correlation 0: real solar irradiation<br>
	 * 1: solar irradiation values are adapted according to electricity
	 * generation capability of solar panels based on temperature (and possible
	 * further influence)
	 */
	IntegerResource sensorType();
}
