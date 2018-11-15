/**
 * Copyright 2011-2018 Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
public interface GenericMultiSensor extends GenericFloatSensor {
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
