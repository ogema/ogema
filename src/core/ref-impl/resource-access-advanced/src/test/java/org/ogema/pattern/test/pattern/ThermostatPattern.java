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
