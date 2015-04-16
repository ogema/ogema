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
import org.ogema.core.model.units.PowerResource;
import org.ogema.model.ranges.PowerRange;
import org.ogema.model.targetranges.PowerTargetRange;

/**
 * A generic power sensor. Use {@link ElectricPowerSensor}, {@link ThermalPowerSensor}
 * and other specialized variants where possible.
 */
public interface PowerSensor extends GenericFloatSensor {

	@NonPersistent
	@Override
	PowerResource reading();

	@Override
	PowerRange ratedValues();

	@Override
	PowerTargetRange settings();

	@Override
	PowerTargetRange deviceSettings();

	@Override
	PowerTargetRange deviceFeedback();

	//	/**
	//	 * Only relevant if power sensor measures a device that operates certain
	//	 * programs
	//	 */
	//	ProgramPowerCurve programPowerCurve();
}
