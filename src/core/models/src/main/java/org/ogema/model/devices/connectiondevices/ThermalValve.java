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
package org.ogema.model.devices.connectiondevices;

import org.ogema.model.actors.MultiSwitch;
import org.ogema.model.connections.ThermalConnection;
import org.ogema.model.prototypes.PhysicalElement;

/** 
 * Valve controlling the energy flow through a {@link ThermalConnection}, such
 * as a valve attached to a heating storage or a radiator valve.
 */
public interface ThermalValve extends PhysicalElement {
	/**
	 * Switch position<br>
	 * If valve can only be shut or open, the valve should be open if set to more than 0.5
	 */
	MultiSwitch setting();

	/** 
	 * The thermal connection that is influenced by the settings of the valve. 
	 */
	ThermalConnection connection();
}
