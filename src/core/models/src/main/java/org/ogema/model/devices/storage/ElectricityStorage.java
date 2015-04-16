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
	 * factor, etc.). For instance, the maximum allowed charge rate (power) is
	 * given in {@link org.ogema.core.model.prototypes.Sensor#ratedValues()
	 * electricityConnection.powerSens.ratedValues}, whereas the maximum allowed
	 * discharge rate can be found in      {@link org.ogema.core.model.commonsensors.ElPowerSens#ratedPowerGeneration()
	 * electricityConnection.powerSens.ratedPowerGeneration}.
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
