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
