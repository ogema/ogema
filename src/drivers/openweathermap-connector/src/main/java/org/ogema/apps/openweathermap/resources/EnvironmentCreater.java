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
package org.ogema.apps.openweathermap.resources;

import java.util.HashMap;
import java.util.Map;

import org.ogema.apps.openweathermap.RoomRad;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.units.LengthResource;
import org.ogema.model.locations.Room;
import org.ogema.model.sensors.SolarIrradiationSensor;

/**
 * 
 * Create,handle OGEMA resource (outside room).
 * 
 * @author brequardt
 */
public class EnvironmentCreater {

	private final OgemaLogger logger;
	private final ApplicationManager appMan;

	private static final int solarLowerLimit = 0;
	private static final int solarUpperLimit = 1500;

	public EnvironmentCreater(ApplicationManager appMan) {
		this.logger = appMan.getLogger();
		this.appMan = appMan;
	}

	public Resource createResource(String name, final String city, final String country) {

		logger.info("create new resource with name: " + name);

		Room environment = appMan.getResourceManagement().createResource(name, Room.class);
		RoomRad pattern = new RoomRad(environment);
		environment.type().<IntegerResource> create().setValue(0);

		pattern.city.<StringResource> create().setValue(city);
		pattern.country.<StringResource> create().setValue(country);
		pattern.tempSens.reading().forecast().create();
		pattern.humiditySens.reading().forecast().create();
		pattern.windSens.direction().reading().forecast().create();
		pattern.windSens.speed().reading().forecast().create();
		pattern.windSens.altitude().<LengthResource> create().setValue(0);
		
		SolarIrradiationSensor irradSens = pattern.irradSensor;
		irradSens.reading().forecast().create();
		irradSens.ratedValues().upperLimit().<FloatResource> create().setValue(solarUpperLimit);
		irradSens.ratedValues().lowerLimit().<FloatResource> create().setValue(solarLowerLimit);

		appMan.getResourcePatternAccess().activatePattern(pattern);
		pattern.tempSens.reading().forecast().activate(false);
		
		return environment;
	}

	public Map<String, Object> getParameters(String name) {

		Map<String, Object> map = new HashMap<String, Object>();

		Room environment = appMan.getResourceAccess().getResource(name);

		map.put("country", environment.location().geographicLocation().getSubResource("country", StringResource.class)
				.getValue());

		map.put("city", environment.location().geographicLocation().getSubResource("city", StringResource.class)
				.getValue());

		return map;

	}
}
