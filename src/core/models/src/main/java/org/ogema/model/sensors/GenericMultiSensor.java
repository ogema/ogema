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

import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.model.ranges.GenericFloatRange;
import org.ogema.model.targetranges.GenericFloatTargetRange;

/**
 * A generic sensor for either a continuous value range or a discrete range with more than two steps (e.g. measuring the
 * current position of a dimmer/multi switch).
 */
public interface GenericMultiSensor extends Sensor {
	/**
	 * Number of switchable steps. If this variable is negative or absent, the switch is assumed to allow for a
	 * continuous range of values.
	 */
	IntegerResource numberOfSteps();

	/**
	 * A value between 0 and 1 - both included - which specifies the currently measured position of the switch. This is
	 * a continuous variable in the case of a continuous operating mode, modeling for instance a percentage value, but
	 * in general the meaning depends on the context. If the switch allows for a discrete number of positions only
	 * (determined by {@link numberOfSteps}), then the steps correspond to particular values in the interval [0,1]. For
	 * instance, if the switch has five allowed positions, they can be represented by the values 0, 0.2, 0.4, 0.6, 0.8,
	 * 1. These values either have no particular physical meaning, in which case they shall be chosen equidistant as in
	 * the 5-step example above (default), but if the steps of the switch correspond to a certain percentage value in an
	 * obvious way, they should be chosen equal to these percentages.
	 */
	@Override
	FloatResource reading();

	/**
	 * Range that the readings can assume. Interpretation of the values is defined by {@link #reading() }.
	 */
	@Override
	GenericFloatRange ratedValues();

	/**
	 * Setpoint to reach via management. For the interpretation of the value see {@link #reading()}.
	 */
	@Override
	GenericFloatTargetRange settings();

	@Override
	GenericFloatTargetRange deviceSettings();

	@Override
	GenericFloatTargetRange deviceFeedback();
}
