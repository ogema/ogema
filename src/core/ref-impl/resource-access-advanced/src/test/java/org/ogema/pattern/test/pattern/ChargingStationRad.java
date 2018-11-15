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
package org.ogema.pattern.test.pattern;

import org.ogema.core.model.Resource;
import org.ogema.core.model.ResourceList;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.model.devices.storage.ChargingPoint;
import org.ogema.model.devices.storage.ElectricityChargingStation;

/**
 * Test-RAD used in the tests in this.
 */
public class ChargingStationRad extends ResourcePattern<ElectricityChargingStation> {

	public ResourceList<ChargingPoint> cps = model.chargingPoints();

	public ChargingStationRad(Resource match) {
		super(match);
	}

}
