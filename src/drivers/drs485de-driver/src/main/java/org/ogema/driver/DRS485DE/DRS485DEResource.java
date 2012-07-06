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
package org.ogema.driver.DRS485DE;

import org.ogema.core.model.units.EnergyResource;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.model.metering.ElectricityMeter;

/**
 * Manage complex ElectricityMeter resource. Cache direct references to values.
 * 
 * @author pau
 * 
 */
public class DRS485DEResource {

	/** top level resource */
	ElectricityMeter meter;

	/** Reference to meter.energyReading() */
	EnergyResource totalEnergy;

	DRS485DEResource(ResourceManagement resourceManager, String name) {

		meter = resourceManager.createResource(name, ElectricityMeter.class);
		meter.addOptionalElement("energyReading");

		totalEnergy = meter.energyReading();
	}

	/**
	 * calls meter.activate()
	 * 
	 * @param b
	 *            recursive
	 */
	void activate(boolean b) {
		meter.activate(b);
	}

	/**
	 * calls meter.deactivate()
	 * 
	 * @param b
	 *            recursive
	 */
	void deactivate(boolean b) {
		meter.deactivate(b);
	}

	/**
	 * calls meter.delete()
	 */
	void delete() {
		meter.delete();
		meter = null;
		totalEnergy = null;
	}
}
