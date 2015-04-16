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
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.model.units.EnergyResource;
import org.ogema.model.ranges.EnergyRange;
import org.ogema.model.targetranges.EnergyTargetRange;

/**
 * Sensor reading for time-integrated power (i.e. Energy).
 */
public interface EnergyAccumulatedSensor extends GenericFloatSensor {

	/**
	 * Accumulated energy since {@link #startTime}.
	 */
	@NonPersistent
	@Override
	EnergyResource reading();

	@Override
	EnergyRange ratedValues();

	@Override
	EnergyTargetRange settings();

	@Override
	EnergyTargetRange deviceSettings();

	@Override
	EnergyTargetRange deviceFeedback();

	/**
	 * Time at which the integration started. This implies that the reading
	 * was zero at this point.
	 */
	TimeResource startTime();
}
