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
package org.ogema.model.devices.vehicles;

import org.ogema.core.model.simple.IntegerResource;
import org.ogema.model.devices.storage.ElectricityStorage;

/**
 * Electric vehicle (car, bus, bike, ...). If the vehicle is available at a 
 * charging station this should be indicated by its {@link #battery() } location
 * pointing to the {@link org.ogema.model.devices.storage.ElectricityChargingStation} it is connected to.
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
