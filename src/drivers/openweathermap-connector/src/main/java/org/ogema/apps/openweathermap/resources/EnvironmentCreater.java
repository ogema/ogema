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
package org.ogema.apps.openweathermap.resources;

import java.util.HashMap;
import java.util.Map;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.core.recordeddata.RecordedDataConfiguration;
import org.ogema.core.recordeddata.RecordedDataConfiguration.StorageType;
import org.ogema.model.locations.Room;
import org.ogema.model.sensors.SolarIrradiationSensor;

/**
 * 
 * Create,handle OGEMA resource (outside room).
 * 
 * @author brequardt
 */
public class EnvironmentCreater {

	static EnvironmentCreater instance = new EnvironmentCreater();
	private OgemaLogger logger;
	private ApplicationManager appMan;

	private static final int solarLowerLimit = 0;
	private static final int solarUpperLimit = 1500;

	private EnvironmentCreater() {

	}

	public void init(ApplicationManager appManager) {

		logger = appManager.getLogger();
		appMan = appManager;

	}

	public static EnvironmentCreater getInstance() {
		return instance;
	}

	public Resource createResource(String name, final String city, final String country) {

		logger.info("create new resource with name: " + name);

		RecordedDataConfiguration cfg = new RecordedDataConfiguration();
		cfg.setStorageType(StorageType.ON_VALUE_CHANGED);

		Room environment = appMan.getResourceManagement().createResource(name, Room.class);

		environment.type().setValue(0);

		environment.location().geographicLocation().create();

		StringResource cityRes = environment.location().geographicLocation().addDecorator("city", StringResource.class);

		cityRes.setValue(city);

		StringResource countryRes = environment.location().geographicLocation()
				.addDecorator("country", StringResource.class);

		countryRes.setValue(country);

		TemperatureResource tempSens = environment.temperatureSensor().reading();
		tempSens.forecast().create();
		tempSens.getHistoricalData().setConfiguration(cfg);

		SolarIrradiationSensor irradSensorX = environment.addDecorator("solarIrradiationSensor",
				SolarIrradiationSensor.class);

		irradSensorX.ratedValues().upperLimit().setValue(solarUpperLimit);

		irradSensorX.ratedValues().lowerLimit().setValue(solarLowerLimit);

		FloatResource humidity = environment.humiditySensor().reading().create();

		humidity.getHistoricalData().setConfiguration(cfg);

		humidity.forecast().create();

		environment.activate(true);

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
