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
package org.ogema.model.devices.generators;

import org.ogema.model.connections.ElectricityConnection;
import org.ogema.model.connections.FluidConnection;

/**
 * Heat pump model.
 */
public interface HeatPump extends HeatGenerator {

	/**
	 * Electricity connection for electric heat pumps.
	 */
	ElectricityConnection electricityConnection();

	/**
	 * Fuel connection for non-electrical heat pumps.
	 */
	FluidConnection fuelConnection();
}
