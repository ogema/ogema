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
package org.ogema.model.devices.sensoractordevices;

import org.ogema.model.actors.OnOffSwitch;
import org.ogema.model.connections.ElectricityConnection;
import org.ogema.model.prototypes.PhysicalElement;

/**
 * Device to be provided by hardware driver. Represents a switching/measuring
 * plug adapter that can be plugged/installed between a single device and its
 * grid connection for switching/measuring purposes. Additional sensors/actors
 * available may be added as decorators.
 */
public interface SingleSwitchBox extends PhysicalElement {

	/**
	 * Electric connection that the plug operates on (contains also sensors)
	 */
	ElectricityConnection electricityConnection();

	/**
	 * On/off switch. "On" means that the connected device is allowed to draw
	 * power, "off" means the electricity connection is effectively cut.
	 */
	OnOffSwitch onOffSwitch();

	/**
	 * Reference to the device that is connected to this switch box.
	 */
	PhysicalElement device();
}
