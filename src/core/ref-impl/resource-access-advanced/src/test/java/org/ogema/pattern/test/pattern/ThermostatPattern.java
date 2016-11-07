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
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.model.devices.buildingtechnology.Thermostat;
import org.ogema.model.devices.connectiondevices.ThermalValve;
import org.ogema.model.sensors.TemperatureSensor;
import org.ogema.model.targetranges.TemperatureTargetRange;

/**
 * A "real-world" pattern that caused some problems with an earlier version
 * of the resource management. 
 */
public class ThermostatPattern extends ResourcePattern<Thermostat> {
	
	private final TemperatureSensor tempSens = model.temperatureSensor();
	
	private final ThermalValve valve = model.valve();
	
	@Existence(required=CreateMode.OPTIONAL)
	public final TemperatureResource temperature = tempSens.reading();
	
	public final TemperatureTargetRange tempSetting = tempSens.settings();

	@Existence(required=CreateMode.OPTIONAL)
	public final TemperatureResource setpoint = tempSens.settings().setpoint();

	@Existence(required=CreateMode.OPTIONAL)
	public final TemperatureResource setpointFB = tempSens.deviceFeedback().setpoint();

	@Existence(required=CreateMode.OPTIONAL)
	public final FloatResource valvePosition = valve.setting().stateFeedback();
	
	/**
	 * Constructor for the access pattern. This constructor is invoked by the framework.
	 */
	public ThermostatPattern(Resource device) {
		super(device);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !obj.getClass().equals(getClass())) 
			return false;
		ThermostatPattern other = (ThermostatPattern) obj;
		return this.model.equalsLocation(other.model);
	}
	
	@Override
	public int hashCode() {
		return model.getLocation().hashCode();
	}
	

}
