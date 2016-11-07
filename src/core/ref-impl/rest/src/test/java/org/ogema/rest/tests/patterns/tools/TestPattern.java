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
package org.ogema.rest.tests.patterns.tools;

import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.model.devices.whitegoods.CoolingDevice;
import org.ogema.model.sensors.TemperatureSensor;

public class TestPattern extends ResourcePattern<CoolingDevice> {

	public TestPattern(Resource match) {
		super(match);
	}
	
	@Access(mode=AccessMode.EXCLUSIVE)
	public final TemperatureSensor tempSens = model.temperatureSensor();

	@Equals(value=290)
	public final TemperatureResource reading = tempSens.reading();
	
	public final StringResource room = model.location().room().name();
	
}
