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
package org.ogema.model.devices.whitegoods;

import org.ogema.core.model.ResourceList;
import org.ogema.model.connections.ElectricityConnection;
import org.ogema.model.prototypes.PhysicalElement;

/** 
 * Kitchen hob.
 */
public interface Hob extends PhysicalElement {

	/**
	 * The plates of the hob. The controls for the plates are contained in
	 * the individual plates' data models.
	 */
	public ResourceList<CookingPlate> plates();

	/** 
	 * Electrical connection and measurement 
	 */
	ElectricityConnection electricityConnection();
}
