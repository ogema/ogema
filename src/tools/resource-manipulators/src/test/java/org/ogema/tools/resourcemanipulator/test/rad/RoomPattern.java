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
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.units.ThermalEnergyCapacityResource;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.model.locations.Room;

/**
 * Creation pattern for a room, including the relevant resources that may be
 * used in SEMIAH
 * 
 * @author Timo Fischer, Fraunhofer IWES
 */
public class RoomPattern extends ResourcePattern<Room> {

	/**
	 * Type of the room.
	 */
	public IntegerResource type = model.type();

	//    private TemperatureSensor tempSens = model.temperatureSensor();    

	//    /**
	//     * Currrent room temperature. Unit: Kelvin.
	//     */
	//    public TemperatureResource temperature = tempSens.reading();
	//    
	//    private TemperatureRange tempTargets = tempSens.settings().targetRange();

	//    /**
	//     * Minimum temperature the room should have.
	//     */
	//    public TemperatureResource minTemp = tempTargets.lowerLimit();
	//    
	//    /**
	//     * Maximum temperature the room should have.
	//     */
	//    public TemperatureResource maxTemp = tempTargets.upperLimit();    

	//    /**
	//     * Heat capacity of the room. Multiplying temperature with heat 
	//     * capacity yields an estimate of the energy contained.
	//     */
	//    public ThermalEnergyCapacityResource heatCapacity = model.heatCapacity();

	/**
	 * Default constructor as required by OGEMA. Should not be changed.
	 */
	public RoomPattern(Resource res) {
		super(res);
	}

	/**
	 * Initialize the newly-created object. Should be done before activation.
	 * @param typeId type of the room as defined in the data model of {@link Room}.
	 */
	public void init(int typeId) {
		type.setValue(typeId);
		//        temperature.setCelsius(20.f);
		//        minTemp.setCelsius(18.f);
		//        maxTemp.setCelsius(22.f);
		//        heatCapacity.setValue(5.71e6f);
	}
}
