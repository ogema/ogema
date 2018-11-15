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
