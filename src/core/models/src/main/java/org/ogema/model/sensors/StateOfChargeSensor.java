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
import org.ogema.core.model.simple.FloatResource;
import org.ogema.model.ranges.GenericFloatRange;
import org.ogema.model.targetranges.GenericFloatTargetRange;

/**
 * Sensor measuring the state of charge of something in relative units, from
 * 0.0 (uncharged) to 1.0 (fully charged). 
 */
public interface StateOfChargeSensor extends GenericFloatSensor {

	/**
	 * State of charge of the storage (0.0=empty, 1.0= fully loaded)<br>
	 * unit: 0.0 .. 1.0
	 */
	@NonPersistent
	@Override
	FloatResource reading();

	@Override
	GenericFloatRange ratedValues();

	/**
	 * Target settings as a state of charge (as in {@link #reading()}).
	 */
	@Override
	GenericFloatTargetRange settings();

	@Override
	GenericFloatTargetRange deviceSettings();

	@Override
	GenericFloatTargetRange deviceFeedback();

	//	/**
	//	 * Maximum rated storage capacity unit: J TODO may be changed, see
	//	 * documentation of class for more info.
	//	 */
	//	EnergyResource maxCapacityEnergy();
}
