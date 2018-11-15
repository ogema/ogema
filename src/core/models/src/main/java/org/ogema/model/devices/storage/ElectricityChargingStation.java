/**
 * Copyright 2011-2018 Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
