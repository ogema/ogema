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
package org.ogema.apps.openweathermap;

import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.model.devices.sensoractordevices.WindSensor;
import org.ogema.model.locations.GeographicLocation;
import org.ogema.model.locations.Room;
import org.ogema.model.sensors.HumiditySensor;
import org.ogema.model.sensors.SolarIrradiationSensor;
import org.ogema.model.sensors.TemperatureSensor;

/**
 * OGEMA rad for outside room.
 * 
 * @author brequardt
 * 
 */
// TODO add identifier for OpenWeatherMap connector - do not use every room in the system with matching subresources
public class RoomRad extends ResourcePattern<Room> {

	@Existence(required = CreateMode.MUST_EXIST)
	public final GeographicLocation location = model.location().geographicLocation();

	@Existence(required = CreateMode.MUST_EXIST)
	public final TemperatureSensor tempSens = model.temperatureSensor();

	@Existence(required = CreateMode.MUST_EXIST)
	public final HumiditySensor humiditySens = model.humiditySensor();
	
	@Existence(required = CreateMode.OPTIONAL)
	public final WindSensor windSens = model.getSubResource("windSensor", WindSensor.class);

	@Existence(required = CreateMode.OPTIONAL)
	public final StringResource city = model.location().geographicLocation().getSubResource("city");

	@Existence(required = CreateMode.OPTIONAL)
	public final StringResource country = model.location().geographicLocation().getSubResource("country");

	@Existence(required = CreateMode.OPTIONAL)
	public final SolarIrradiationSensor irradSensor = model.getSubResource("solarIrradiationSensor",
			SolarIrradiationSensor.class);

	public RoomRad(Resource match) {
		super(match);
	}

}
