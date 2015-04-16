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

import org.ogema.model.actors.MultiSwitch;
import org.ogema.model.actors.OnOffSwitch;
import org.ogema.model.prototypes.PhysicalElement;

/**
 * Electrical dimmer device.
 */
public interface ElectricDimmer extends PhysicalElement {

	/**
	 * To switch dimmer on and off (not the device controlled by the dimmer,
	 * which should have its own switch).
	 */
	OnOffSwitch onOffSwitch();

	/**
	 * The dimming switch of the dimmer. The value 1 of shall represent the
	 * undimmed situation (on), 0 the fully dimmed state (off).
	 */
	MultiSwitch setting();

	/**
	 * Device controlled by the dimmer.
	 */
	PhysicalElement device();
}
