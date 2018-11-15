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

import org.ogema.core.model.simple.FloatResource;
import org.ogema.model.actors.MultiSwitch;
import org.ogema.model.sensors.StateOfChargeSensor;
import org.ogema.model.prototypes.PhysicalElement;
import org.ogema.model.ranges.EnergyRange;
import org.ogema.model.ranges.PowerRange;

/** Base class for an energy storage. */
public interface EnergyStorage extends PhysicalElement {

	/**
	 * Setting for the total charging or discharging, as a share on {@link #ratedPower()}.
	 * Values from zero to plus one refer to charging with a respective share of the maximum
	 * charging power. Negative values between minus one and zero refer to dis-charging with
	 * the respective share of the maximum discharging power (with negative one meaning full
	 * discharge). The related sensors for charging and discharging are in the respective
	 * connections of the device. <br>
	 * Not all types of storage may provide this actor. For complex storages with multiple
	 * connections and no smart internal control the connections leading to the storage may
	 * have to be controlled individually.
	 */
	MultiSwitch setting();

	/**
	 * Maximum and minimum charge and discharge powers. Positive values refer
	 * to charging the storage, negative to discharging.
	 */
	PowerRange ratedPower();

	/**
	 * State of charge sensor and storage information. <br>
	 * Also contains information on alarm limits, like a minimum storage reserve for batteries, in 
	 * {@link org.ogema.model.sensors.Sensor#settings()}.
	 */
	StateOfChargeSensor chargeSensor();

	/**
	 * Rated minimum and maximum energy that can be stored in the storage. The {@link #chargeSensor()}
	 * refers to these limits.
	 */
	EnergyRange ratedEnergy();

	/**
	 * Self discharge rate at full charge. <br>
	 * Unit: Full charge per second.
	 */
	FloatResource selfDischargeRate();
}
