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

import org.ogema.core.model.ModelModifiers.NonPersistent;
import org.ogema.core.model.units.VelocityResource;
import org.ogema.model.ranges.VelocityRange;
import org.ogema.model.targetranges.VelocityTargetRange;

/**
 * Sensor measuring the velocity of an object, a flowing fluid or a flowing gas.
 */
public interface VelocitySensor extends GenericFloatSensor {

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
