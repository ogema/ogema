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
