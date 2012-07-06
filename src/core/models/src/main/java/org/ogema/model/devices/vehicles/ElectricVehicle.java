/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ogema.model.devices.vehicles;

import org.ogema.core.model.simple.IntegerResource;
import org.ogema.model.devices.storage.ElectricityStorage;

/**
 * Electric vehicle (car, bus, bike, ...). If the vehicle is available at a 
 * charging station this should be indicated by its {@link #battery() } location
 * pointing to the {@link ElectricityChargingStation} it is connected to.
 */
public interface ElectricVehicle extends Vehicle {

	/**
	 * 0: fully electric: Runs on electrical power, only
	 * 1: hybrid: Vehicle has at least one other motor in addition to the electrical one.
	 */
	IntegerResource type();

	/**
	 * Battery built into the vehicle.
	 */
	ElectricityStorage battery();
}
