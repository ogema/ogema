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
import org.ogema.core.model.schedule.AbsoluteSchedule;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.model.locations.Building;
import org.ogema.model.connections.ElectricityConnection;
import org.ogema.model.sensors.PowerSensor;

/**
 * Test-RAD used in the tests in this. Keep this one in this package, since
 * access to private, public and package-private fields is explicitly tested on this.
 */
public class BuildingRad extends ResourcePattern<Building> {

	public final ElectricityConnection elConn = model.electricityConnectionBox().connection();
	private PowerSensor powerSensor = elConn.powerSensor();

	@Access(mode = AccessMode.EXCLUSIVE)
	public final FloatResource powerReading = powerSensor.reading();

	public final AbsoluteSchedule powerForecast = powerReading.forecast();

	@Existence(required = CreateMode.OPTIONAL)
	public final IntegerResource connType = elConn.connectionType();

	private StringResource sensorName = powerSensor.getSubResource("name", StringResource.class);

	public BuildingRad(Resource match) {
		super(match);
	}

	public final boolean requiredFieldsExist() {
		return (model.exists() && elConn.exists() && powerSensor.exists() && powerReading.exists()
				&& powerForecast.exists() && sensorName.exists());
	}

	// ! Returns true if all fields are active.
	public final boolean requiredFieldsActive() {
		return (model.isActive() && elConn.isActive() && powerSensor.isActive() && powerReading.isActive()
				&& powerForecast.exists() && sensorName.exists());
	}

	// ! Returns true if all fields are inactive.
	public final boolean requiredFieldsInactive() {
		final boolean anyActive = (((Resource) model).isActive() || elConn.isActive() || powerSensor.isActive()
				|| powerReading.isActive() || powerForecast.isActive() || sensorName.isActive());
		return !anyActive;
	}

	// ! Returns true if all of the required fields are not null.
	public final boolean requiredFieldsNotNull() {
		final boolean anyRequiredFieldNull = (model == null || elConn == null || powerSensor == null
				|| powerReading == null || powerForecast == null || sensorName == null);
		return !anyRequiredFieldNull;
	}

}
