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
package org.ogema.model.actors;

import org.ogema.core.model.ModelModifiers.NonPersistent;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.model.functions.RealFunction;
import org.ogema.model.ranges.GenericFloatRange;
import org.ogema.model.targetranges.GenericFloatTargetRange;

/**
 * Generic Multi-step-switch. This resource type is heavily used in the
 * modelling of state controls for devices whose power level can be adjusted
 * more detailed than only on/off.
 */
public interface MultiSwitch extends Actor {

	/**
	 * Number of switchable steps. If this variable is negative or absent, the
	 * switch is assumed to allow for a continuous range of setpoints.
	 */
	IntegerResource setpointNum();

	/**
	 * A value between 0 and 1 - both included - which specifies the currently
	 * selected switching setpoint. This is a continuous variable in the case of
	 * a smooth control switch, modeling for instance a percentage value, but in
	 * general the meaning depends on the context. If the switch allows for a
	 * discrete number of positions only (determined by {@link setpointNum}),
	 * then the steps correspond to particular values in the interval [0,1]. For
	 * instance, if the switch has five allowed positions, they can be
	 * represented by the values 0, 0.2, 0.4, 0.6, 0.8, 1. These values either
	 * have no particular physical meaning, in which case they shall be chosen
	 * equidistant as in the 5-step example above (default), but if the steps of
	 * the switch correspond to a certain percentage value in an obvious way,
	 * they should be chosen equal to these percentages. <br>
	 * If, in the discrete case, the actual value lies between two allowed
	 * steps, applications shall assume the closest allowed lower value to be
	 * selected.<br>
	 * This is the setpoint, the actual measured value is {@link #stateFeedback()}.
	 */
	@Override
	FloatResource stateControl();

	/**
	 * The actual measured value. See {@link #stateControl()}.
	 */
	@Override
	@NonPersistent
	FloatResource stateFeedback();

	@Override
	GenericFloatRange ratedValues();

	@Override
	GenericFloatTargetRange settings();

	/**
	 * Net electrical power of the device as a function of the {@link #stateControl()}. The
	 * sign convention of the power is determined by the device's electrical connection.
	 * Since electrical connections are usually modeled with the device being the
	 * output, positive values mean energy consumption, negatives mean energy generation.
	 */
	RealFunction electricPowerFunction();

	/**
	 * Net thermal power of the device as a function of the {@link #stateControl() }. The
	 * sign convention of the power is determined by the device's thermal connection.
	 */
	RealFunction thermalPowerFunction();
}
