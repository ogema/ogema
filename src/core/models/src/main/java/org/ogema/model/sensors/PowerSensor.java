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
