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
package org.ogema.pattern.test;

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

	final ElectricityConnection elConn = model.electricityConnectionBox().connection();
	private PowerSensor powerSensor = elConn.powerSensor();

	@Access(mode = AccessMode.EXCLUSIVE)
	FloatResource powerReading = powerSensor.reading();

	protected AbsoluteSchedule powerForecast = powerReading.forecast();

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
	boolean requiredFieldsNotNull() {
		final boolean anyRequiredFieldNull = (model == null || elConn == null || powerSensor == null
				|| powerReading == null || powerForecast == null || sensorName == null);
		return !anyRequiredFieldNull;
	}

}
