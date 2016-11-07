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
package org.ogema.pattern.test.pattern;

import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.model.devices.buildingtechnology.Thermostat;
import org.ogema.model.locations.Room;
import org.ogema.model.sensors.TemperatureSensor;

public class ChangeListenerPattern extends ResourcePattern<Thermostat> {

	public ChangeListenerPattern(Resource match) {
		super(match);
	}
	
	public final TemperatureSensor tempSens = model.temperatureSensor();
	
	// by default, the value listener is activated
	@ChangeListener
	public final TemperatureResource reading = tempSens.reading();
	
	@ChangeListener(structureListener=true)
	@Existence(required=CreateMode.OPTIONAL)
	public final Room room = model.location().room(); 

	@ChangeListener(structureListener=true,valueListener=true)
	@Existence(required=CreateMode.OPTIONAL)
	public final StringResource name = model.name();
	
	@ChangeListener(structureListener=true)
	public final TemperatureSensor nullSensor = null;

}
