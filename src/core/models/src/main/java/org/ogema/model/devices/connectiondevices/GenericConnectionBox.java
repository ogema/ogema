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
package org.ogema.model.devices.connectiondevices;

import org.ogema.model.locations.Building;
import org.ogema.model.locations.BuildingPropertyUnit;
import org.ogema.model.prototypes.Connection;
import org.ogema.model.prototypes.PhysicalElement;

/**
 * Connection and/or distribution element for a commodity, for example an electric distribution box, a thermal circuit of water
 * circuit branching point etc.
 */
public interface GenericConnectionBox extends PhysicalElement {
	/**
	 * The connection that this box represents and acts on.
	 */
	Connection connection();

	/**
	 * Reference to building property unit which the connection box connects a public grid, if applicable
	 */
	BuildingPropertyUnit propertyUnit();

	/**
	 * Reference to building which the connection box connects a public grid to, if applicable
	 */
	Building building();
}
