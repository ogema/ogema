/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
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
import org.ogema.core.model.units.VelocityResource;
import org.ogema.model.ranges.VelocityRange;
import org.ogema.model.targetranges.VelocityTargetRange;

/**
 * Sensor measuring the velocity of an object, a flowing fluid or a flowing gas.
 */
public interface VelocitySensor extends Sensor {

	/**
	 * Current velocity of the object/substance. The direction of the positive
	 * sign must be defined in the parent resource.
	 */
	@NonPersistent
	@Override
	VelocityResource reading();

	/**
	 * Rated values for the velocity of the substance/gas.The direction of the positive
	 * sign must be defined in the parent resource.
	 */
	@Override
	VelocityRange ratedValues();

	/**
	 * Settings for the velocity of the substance/gas.The direction of the positive
	 * sign must be defined in the parent resource.
	 */
	@Override
	VelocityTargetRange settings();

	/**
	 * Device settings for the velocity of the substance/gas.The direction of the positive
	 * sign must be defined in the parent resource.
	 */
	@Override
	VelocityTargetRange deviceSettings();

	/**
	 * Device settings' feedbacks for the velocity of the substance/gas.The direction of the positive
	 * sign must be defined in the parent resource.
	 */
	@Override
	VelocityTargetRange deviceFeedback();
}
