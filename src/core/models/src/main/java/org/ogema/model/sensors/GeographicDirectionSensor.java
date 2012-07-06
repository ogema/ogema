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
import org.ogema.core.model.units.AngleResource;
import org.ogema.model.ranges.AngleRange;
import org.ogema.model.targetranges.AngleTargetRange;

/**
 * GenericFloatSensor determining any geographic direction. In case a movement
 * is measured, usually the direction from which the flow comes is used (but
 * details should be provided with the parent resource)
 */
public interface GeographicDirectionSensor extends Sensor {

	/**
	 * Direction <br>
	 * unit: ° (angle degree; zero=north; 90°=east, 180°=south, 270°=west)
	 */
	@NonPersistent
	@Override
	AngleResource reading();

	/**
	 * Technical constraints for the angle.<br>
	 * unit: ° (angle degree; zero=north; 90°=east, 180°=south, 270°=west)
	 */
	@Override
	AngleRange ratedValues();

	/**
	 * Direction settings. Interpreation of the value is identical to
	 * {@link #reading()}.<br>
	 * unit: ° (angle degree; zero=north; 90°=east, 180°=south, 270°=west)
	 */
	@Override
	AngleTargetRange settings();

	/**
	 * Direction settings to be sent to device. Interpreation of the value is identical to
	 * {@link #reading()}.<br>
	 * unit: ° (angle degree; zero=north; 90°=east, 180°=south, 270°=west)
	 */
	@Override
	AngleTargetRange deviceSettings();

	/**
	 * Device direction settings feedback. Interpreation of the value is identical to
	 * {@link #reading()}.<br>
	 * unit: ° (angle degree; zero=north; 90°=east, 180°=south, 270°=west)
	 */
	@Override
	AngleTargetRange deviceFeedback();

}
