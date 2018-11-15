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
package org.ogema.tools.resourcemanipulator.test.rad;

import org.ogema.core.model.Resource;
import org.ogema.core.model.schedule.AbsoluteSchedule;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.core.resourcemanager.pattern.ResourcePattern.CreateMode;
import org.ogema.core.resourcemanager.pattern.ResourcePattern.Existence;
import org.ogema.model.ranges.TemperatureRange;
import org.ogema.model.sensors.TemperatureSensor;
import org.ogema.model.targetranges.TemperatureTargetRange;

/**
 * Resource pattern describing the temperature sensor devices used in SEMIAH.
 * 
 * @author Timo Fischer, Fraunhofer IWES
 */
public class TemperatureSensorPattern extends ResourcePattern<TemperatureSensor> {

	@Existence(required = CreateMode.OPTIONAL)
	public final TemperatureTargetRange settings = model.settings();

	@Existence(required = CreateMode.OPTIONAL)
	public final TemperatureRange controlLimits = settings.controlLimits();

	@Existence(required = CreateMode.OPTIONAL)
	public final TemperatureResource lowerLimit = controlLimits.lowerLimit();

	@Existence(required = CreateMode.OPTIONAL)
	public final AbsoluteSchedule lowerLimitProgram = lowerLimit.program();

	@Existence(required = CreateMode.OPTIONAL)
	public final TemperatureResource upperLimit = controlLimits.upperLimit();

	@Existence(required = CreateMode.OPTIONAL)
	public final AbsoluteSchedule upperLimitProgram = upperLimit.program();

	/**
	 * Default constructor required by OGEMA framework. Do not change.
	 */
	public TemperatureSensorPattern(Resource res) {
		super(res);
	}
}
