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
package org.ogema.model.devices.whitegoods;

import org.ogema.model.actors.OnOffSwitch;
import org.ogema.model.connections.ElectricityConnection;
import org.ogema.model.prototypes.PhysicalElement;
import org.ogema.model.ranges.PowerRange;

/** 
 * Dish washer.
 */
public interface DishWasher extends PhysicalElement {

	/** 
	 * Switch to control when the device draws electrical power to wash. 
	 */
	OnOffSwitch onOffSwitch();

	/** 
	 * Electrical connection and measurement 
	 */
	ElectricityConnection electricityConnection();

	/**
	 * Rated power for the device.
	 */
	PowerRange ratedPower();
}
