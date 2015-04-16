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
