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
package org.ogema.model.devices.buildingtechnology;

import org.ogema.model.actors.OnOffSwitch;
import org.ogema.model.actors.MultiSwitch;
import org.ogema.model.connections.ElectricityConnection;
import org.ogema.model.prototypes.PhysicalElement;
import org.ogema.model.ranges.PowerRange;

/**
 * Ventilator / mechanical fan
 */
public interface MechanicalFan extends PhysicalElement {

	/**
	 * Switch to control when the air conditioning draws electrical power to
	 * generate cold
	 */
	OnOffSwitch onOffSwitch();

	/**
	 * Power setting relative to maximum powers.
	 */
	MultiSwitch setting();

	/**
	 * electrical connection and measurement
	 */
	ElectricityConnection electricityConnection();

	/**
	 * Rated power of the device.
	 */
	PowerRange ratedPower();
}
