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
import org.ogema.model.prototypes.PhysicalElement;
import org.ogema.model.sensors.GeographicDirectionSensor;

/**
 * A window blind.
 */
public interface Blind extends PhysicalElement {

	/**
	 * Specifies the current state of the blind (
	 * {@link org.ogema.model.actors.MultiSwitch#stateFeedback() setting.stateFeedback}
	 * : 0: open, 1: fully closed), as well as a setpoint
	 * if automatic control is possible.
	 */
	MultiSwitch setting();

	/**
	 * Orientation in which the window is facing.
	 */
	GeographicDirectionSensor windowOrientation();
}
