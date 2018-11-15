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

import org.ogema.core.model.units.VolumeResource;
import org.ogema.model.actors.MultiSwitch;
import org.ogema.model.actors.OnOffSwitch;
import org.ogema.model.connections.ElectricityConnection;
import org.ogema.model.sensors.TemperatureSensor;
import org.ogema.model.prototypes.PhysicalElement;
import org.ogema.model.ranges.PowerRange;

/** 
 * Fridge, Freezer (any device with a single cold storage).
 */
public interface CoolingDevice extends PhysicalElement {

	/**
	 * Switch to control when the device draws electrical power to generate cold. If the device has separate switches to
	 * control when cooling fluid is used for actual cooling another switch model should be used.
	 */
	OnOffSwitch onOffSwitch();

	/**
	 * Power setting.
	 */
	MultiSwitch setting();

	/** 
	 * Electrical connection and measurement 
	 */
	ElectricityConnection electricityConnection();

	/**
	 * Rated power
	 */
	PowerRange ratedPower();

	/**
	 * Inside temperature of the device.
	 */
	TemperatureSensor temperatureSensor();

	/**
	 * Total volume of the cooling space.
	 */
	VolumeResource volume();
}
