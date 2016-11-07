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
package org.ogema.apps.openweathermap;

import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.model.locations.GeographicLocation;
import org.ogema.model.locations.Room;
import org.ogema.model.sensors.HumiditySensor;
import org.ogema.model.sensors.SolarIrradiationSensor;
import org.ogema.model.sensors.TemperatureSensor;
/**
 * OGEMA rad for outside room.
 * @author brequardt
 *
 */
// TODO add identifier for OpenWeatherMap connector - do not use every room in the system with matching subresources
public class RoomRad extends ResourcePattern<Room> {

	@Existence(required = CreateMode.MUST_EXIST)
	public final GeographicLocation location = model.location()
			.geographicLocation();

	@Existence(required = CreateMode.MUST_EXIST)
	public final TemperatureSensor tempSens = model.temperatureSensor();

	@Existence(required = CreateMode.MUST_EXIST)
	public final HumiditySensor humiditySens = model.humiditySensor();

	@Existence(required = CreateMode.OPTIONAL)
	public final StringResource city = model.location().geographicLocation()
			.getSubResource("city");

	@Existence(required = CreateMode.OPTIONAL)
	public final StringResource country = model.location().geographicLocation()
			.getSubResource("country");
	
	@Existence(required = CreateMode.OPTIONAL)
	public final SolarIrradiationSensor irradSensor = model.getSubResource(
			"solarIrradiationSensor", SolarIrradiationSensor.class);
	
	public RoomRad(Resource match) {
		super(match);
	}

}
