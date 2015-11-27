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
package org.ogema.model.devices.storage;

import org.ogema.core.model.ResourceList;
import org.ogema.model.connections.ElectricityConnection;
import org.ogema.model.prototypes.PhysicalElement;
import org.ogema.model.ranges.PowerRange;
import org.ogema.model.smartgriddata.ElectricEnergyRequest;

/**
 * Describes a stationary charging station, such as a charging point for an
 * electric vehicle.
 */
public interface ElectricityChargingStation extends PhysicalElement {

	/**
	 * Electrical connection of the charging station to the grid. The
	 * electrical connections to the connected batteries are modeled by the
	 * electrical connections in the batteries.
	 */
	ElectricityConnection electricityConnection();

	/**
	 * Total rated power of the charging station. Individual charging points 
	 * have individual rated powers, see {@link ChargingPoint#ratedPower()}.
	 */
	PowerRange ratedPower();

	/**
	 * Actual charging points of this charging station. 
	 */
	ResourceList<ChargingPoint> chargingPoints();

	/**
	 * External energy requests, for instance from the grid operator, the balancing group manager,
	 * etc. This does not include the requests from the connected vehicles, which are modeled in 
	 * {@link ChargingPoint#energyRequest() chargingPoints.get(X).energyRequest}.
	 */
	ResourceList<ElectricEnergyRequest> energyRequests();

}
