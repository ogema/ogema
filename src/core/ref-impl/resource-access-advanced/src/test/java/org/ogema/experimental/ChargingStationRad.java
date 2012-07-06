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
package org.ogema.experimental;

import org.ogema.core.model.Resource;
import org.ogema.core.model.ResourceList;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.model.devices.storage.ElectricityChargingStation;
import org.ogema.model.smartgriddata.ElectricEnergyRequest;

/**
 * Test-RAD used in the tests in this.
 */
public class ChargingStationRad extends ResourcePattern<ElectricityChargingStation> {

	ResourceList<ElectricEnergyRequest> requests = model.energyRequests();

	public ChargingStationRad(Resource match) {
		super(match);
	}

}
