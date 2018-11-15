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

import java.util.ArrayList;
import java.util.List;

import org.ogema.apps.openweathermap.dao.ForecastData;
import org.ogema.apps.openweathermap.dao.OpenWeatherMapREST;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.channelmanager.measurements.DoubleValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.core.timeseries.InterpolationMode;

/**
 * 
 * Access to the OGEMA resources (forecasts).
 * 
 * @author brequardt
 */
public class ResourceUtil {

	private final Schedule irradiationForecast;
	private final Schedule temperatureForecast;
	private final ApplicationManager appMan;

	public ResourceUtil(ApplicationManager appMan, TemperatureResource tempResouce, FloatResource irrad) {

		temperatureForecast = tempResouce.forecast().create();

		irradiationForecast = irrad.forecast().create();

		tempResouce.forecast().setInterpolationMode(InterpolationMode.LINEAR);
		irrad.forecast().setInterpolationMode(InterpolationMode.LINEAR);
		tempResouce.activate(true);
		irrad.activate(true);
		this.appMan = appMan;
	}

	/**
	 * calculate/interpolate weather information (temperature,solarirradiation)
	 * 
	 * @param city
	 *            name of the city
	 * @param county
	 *            shortcut of the country
	 */
	public void update(String city, String county) {

		ForecastData data = OpenWeatherMapREST.getInstance().getWeatherForcast(city, county);
		int intervallInMinutes = 1;
		data = WeatherUtil.getInstance().interpolateForecast(data, intervallInMinutes);

		List<SampledValue> irradiationList = new ArrayList<>();
		List<SampledValue> tempList = new ArrayList<>();
		appMan.getLogger().debug("got {} values for {}/{}", data.getList().size(), county, city);

		for (org.ogema.apps.openweathermap.dao.List entry : data.getList()) {

			// DateTime c = new DateTime(entry.getDt());
			SampledValue irrad = newSampledDouble(entry.getIrradiation(), entry.getDt());
			SampledValue temp = newSampledDouble(entry.getMain().getTemp(), entry.getDt());
			irradiationList.add(irrad);
			tempList.add(temp);
			// appMan.getLogger().info(c + " " + temp);
			// appMan.getLogger().info(c + " " + irrad);
		}

		irradiationForecast.addValues(irradiationList);
		temperatureForecast.addValues(tempList);
		appMan.getLogger().debug("wrote {} values to {}", irradiationList.size(), irradiationForecast.getPath());
		appMan.getLogger().debug("wrote {} values to {}", tempList.size(), temperatureForecast.getPath());
	}

	private SampledValue newSampledDouble(Double value, long timestamp) {
		DoubleValue c = new DoubleValue(value);
		SampledValue e = new SampledValue(c, timestamp, Quality.GOOD);
		return e;
	}
}
