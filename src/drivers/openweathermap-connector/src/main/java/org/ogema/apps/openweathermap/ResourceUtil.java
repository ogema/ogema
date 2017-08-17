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
