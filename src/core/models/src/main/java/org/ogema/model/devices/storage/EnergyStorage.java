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
	 * Also contains information on alarm limits, like a minimum storage reserve for batteries, in {@link
	 * org.ogema.core.model.prototypes.Sensor.#alarmLimits() chargeSensor.alarmLimits.lowerLimit}, and control limits, in
	 * {@link org.ogema.core.model.prototypes.Sensor.#controlLimits() chargeSensor.controlLimits}.
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
