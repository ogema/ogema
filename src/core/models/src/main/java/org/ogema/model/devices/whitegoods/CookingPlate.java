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
package org.ogema.model.devices.whitegoods;

import org.ogema.model.actors.MultiSwitch;
import org.ogema.model.connections.ElectricityConnection;
import org.ogema.model.sensors.TemperatureSensor;
import org.ogema.model.prototypes.PhysicalElement;
import org.ogema.model.ranges.PowerRange;

/** 
 * Cooking plate.
 */
public interface CookingPlate extends PhysicalElement {
	/** 
	 * Temperature measurement and setting for the plate. 
	 */
	TemperatureSensor temperatureSensor();

	/** 
	 * Power setting for the plate. 
	 */
	MultiSwitch setting();

	/** 
	 * Electrical connection and measurement 
	 */
	ElectricityConnection electricityConnection();

	/**
	 * Rated power.
	 */
	PowerRange ratedPower();
}
