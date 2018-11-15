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
package org.ogema.model.devices.storage;

import org.ogema.core.model.simple.IntegerResource;
import org.ogema.model.connections.ElectricityConnection;
import org.ogema.model.sensors.TemperatureSensor;
import org.ogema.model.sensors.ElectricVoltageSensor;

/**
 * Storage unit for electrical energy, such as a battery.
 */
public interface ElectricityStorage extends EnergyStorage {

	/**
	 * 1 : Generic battery <br>
	 * 2 : Generic redox flow battery <br>
	 * 3 : Generic capacitor <br>
	 * 4 : Generic mechanical storage <br>
	 * 5 : Generic chemical storage <br>
	 * 6 : Generic thermal storage for electrical energy<br>
	 *
	 * 10 : Lithium-ion battery <br>
	 * 11 : Lead-acid battery <br>
	 * 12 : VRLA battery (valve-regulated lead–acid) <br>
	 * 13 : Sodium-sulfur battery <br>
	 * 14 : Nickel-iron battery <br>
	 *
	 * 20 : Vanadium redox flow battery <br>
	 * 21 : Zink bromine flow battery <br>
	 *
	 * 40 : Flywheel <br>
	 * 41 : Pumped-storage hydro<br>
	 * 42 : Compressed air <br>
	 *
	 * 50 : Hydrogen <br>
	 * 51 : Methane (synthetic natural gas) <br>
	 *
	 * 60 : Molten salt <br>
	 *
	 * 9999 : Other <br>
	 *
	 */
	IntegerResource type();

	/**
	 * Electrical connection of the storage. <br>
	 * Also contains information on rated values (power, voltage, current, power
	 * factor, etc.), in {@link org.ogema.model.sensors.Sensor#ratedValues()}. 
	 * For instance, the maximum allowed charge rate (power) is given in 
	 * {@link org.ogema.model.sensors.Sensor#ratedValues() electricityConnection.powerSensor().ratedValues()}.
	 * <br>
	 * If the storage device is a battery with an integrated inverter, this connection shall be interpreted as
	 * the AC connection of the inverter. The DC connection is modeled in the inverter.
	 */
	ElectricityConnection electricityConnection();
	
	/**
	 * Number of charge cycles so far.
	 */
	IntegerResource cycles();

	/**
	 * Estimated number of charge cycles before considerable loss of capacity
	 * occurs.
	 */
	IntegerResource maximumCycles();

	/**
	 * Operating temperature, relevant only to a limited set of storage types.
	 */
	TemperatureSensor temperatureSensor();

	/**
	 * Battery internal voltage.
	 */
	ElectricVoltageSensor internalVoltage();
}
