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
import org.ogema.core.model.units.VolumeResource;
import org.ogema.model.ranges.VolumeRange;
import org.ogema.model.targetranges.VolumeTargetRange;

/**
 * Sensor reading for time-integrated flow (i.e. total volume).
 */
public interface VolumeAccumulatedSensor extends GenericFloatSensor {

	/**
	 * Accumulated volume since {@link #startTime()}.
	 */
	@NonPersistent
	@Override
	VolumeResource reading();

	@Override
	VolumeRange ratedValues();

	@Override
	VolumeTargetRange settings();

	@Override
	VolumeTargetRange deviceSettings();

	@Override
	VolumeTargetRange deviceFeedback();

	/**
	 * Time at which the integration started. This implies that the reading was
	 * zero at this point.
	 */
	TimeResource startTime();
}
