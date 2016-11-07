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
import org.ogema.core.model.units.AngleResource;
import org.ogema.model.ranges.AngleRange;
import org.ogema.model.targetranges.AngleTargetRange;

/**
 * Angle sensor measuring an angle, e.g. a geometrical angle or an angle between wave forms such as
 * power angles.
 */
public interface AngleSensor extends GenericFloatSensor {

	@NonPersistent
	@Override
	AngleResource reading();

	@Override
	AngleRange ratedValues();

	@Override
	AngleTargetRange settings();

	@Override
	AngleTargetRange deviceSettings();

	@Override
	AngleTargetRange deviceFeedback();
}
