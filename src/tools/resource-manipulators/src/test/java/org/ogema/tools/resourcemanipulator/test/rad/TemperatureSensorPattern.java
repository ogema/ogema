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
