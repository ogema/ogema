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
import org.ogema.core.model.schedule.DefinitionSchedule;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.core.resourcemanager.pattern.ResourcePattern.CreateMode;
import org.ogema.core.resourcemanager.pattern.ResourcePattern.Existence;
import org.ogema.model.locations.Room;
import org.ogema.model.ranges.TemperatureRange;
import org.ogema.model.sensors.TemperatureSensor;
import org.ogema.model.targetranges.TemperatureTargetRange;

public class MyRoomPattern extends ResourcePattern<Room> {

	public MyRoomPattern(Resource match) {
		super(match);
	}

	@Existence(required = CreateMode.MUST_EXIST)
	public final TemperatureSensor temperatureSensor = model.temperatureSensor();

	@Existence(required = CreateMode.OPTIONAL)
	public final TemperatureTargetRange settings = temperatureSensor.settings();

	@Existence(required = CreateMode.OPTIONAL)
	public final TemperatureRange controlLimits = settings.controlLimits();

	@Existence(required = CreateMode.OPTIONAL)
	public final TemperatureResource lowerLimit = controlLimits.lowerLimit();

	@Existence(required = CreateMode.OPTIONAL)
	public final DefinitionSchedule lowerLimitProgram = lowerLimit.program();

	@Existence(required = CreateMode.OPTIONAL)
	public final TemperatureResource upperLimit = controlLimits.upperLimit();

	@Existence(required = CreateMode.OPTIONAL)
	public final DefinitionSchedule upperLimitProgram = upperLimit.program();
}
