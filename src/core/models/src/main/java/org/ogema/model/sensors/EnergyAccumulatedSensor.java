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
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.model.units.EnergyResource;
import org.ogema.model.ranges.EnergyRange;
import org.ogema.model.targetranges.EnergyTargetRange;

/**
 * Sensor reading for time-integrated power (i.e. Energy).
 */
public interface EnergyAccumulatedSensor extends GenericFloatSensor {

	/**
	 * Accumulated energy since {@link #startTime}.
	 */
	@NonPersistent
	@Override
	EnergyResource reading();

	@Override
	EnergyRange ratedValues();

	@Override
	EnergyTargetRange settings();

	@Override
	EnergyTargetRange deviceSettings();

	@Override
	EnergyTargetRange deviceFeedback();

	/**
	 * Time at which the integration started. This implies that the reading
	 * was zero at this point.
	 */
	TimeResource startTime();
}
