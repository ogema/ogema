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
 * Fridge/Freezer combination. This models devices with a cooling space and a
 * freezing space that share a common electricity generator.
 */
public interface FreezeCombi extends PhysicalElement {

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
	 * Electrical electricityConnection and measurement.
	 */
	ElectricityConnection electricityConnection();

	/**
	 * Rated power.
	 */
	PowerRange ratedPower();

	/**
	 * Temperature of the cooling part of the device.
	 */
	TemperatureSensor temperatureSensorCooling();

	/**
	 * Temperature of the freezing part of the device.
	 */
	TemperatureSensor temperatureSensorFreezing();

	/**
	 * Cooling space size
	 */
	VolumeResource volumeCooling();

	/**
	 * Freezing space size
	 */
	VolumeResource volumeFreezing();
}
