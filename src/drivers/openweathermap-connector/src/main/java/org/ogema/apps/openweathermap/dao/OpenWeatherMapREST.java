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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ogema.apps.openweathermap.dao;

import org.ogema.apps.openweathermap.OpenWeatherMapApplication;
import org.ogema.apps.openweathermap.WeatherUtil;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author skarge
 */
public class OpenWeatherMapREST {

	private String API_KEY = "";
	private final String BASE_URL = "http://api.openweathermap.org/";
	private final WeatherUtil util = WeatherUtil.getInstance();
	private static OpenWeatherMapREST instance;

	public static void main(String[] args) {
		// System.setProperty("openweathermapKEY", "");
		OpenWeatherMapREST rest = OpenWeatherMapREST.getInstance();
		ForecastData result = rest.getWeatherForcast("Kassel", "de");

		int intervallInMinutes = 1;
		result = rest.util.interpolateForecast(result, intervallInMinutes);
		System.out.println(rest.util.toString(result) + "--------------------\n");
		CurrentData current = rest.getWeatherCurrent("Kassel", "de");
		System.out.println(rest.util.toString(current));
	}

	public void setAPI_KEY(String API_KEY) {
		this.API_KEY = API_KEY;
	}

	public static OpenWeatherMapREST getInstance() {
		if (OpenWeatherMapREST.instance == null) {
			OpenWeatherMapREST.instance = new OpenWeatherMapREST();
			String key = System.getProperty("org.ogema.drivers.openweathermap.key", null);
			if (key == null) {
				System.out.print("openweathermapKEY is required, Please register: http://openweathermap.org/register");
			}
			else {
				OpenWeatherMapREST.instance.setAPI_KEY(key);
			}
		}

		return OpenWeatherMapREST.instance;
	}

	public ForecastData getWeatherForcast(String city, String countryCode) {
		String url = BASE_URL + "data/2.5/forecast?q=" + city + "," + countryCode;
		if (API_KEY != null && API_KEY.isEmpty() == false) {
			url += "&APPID=" + API_KEY;
		}
		String json = util.call(url);
		ObjectMapper mapper = new ObjectMapper();
		try {
			ForecastData data = mapper.readValue(json, ForecastData.class);
			return data;
		} catch (Exception e) {
			System.out.println("error by: " + e.getMessage() + " --> " + json + "\n\n");
			e.printStackTrace();
			return null;
		}
	}

	public CurrentData getWeatherCurrent(String city, String countryCode) {
		// city = "Kassel";
		// countryCode = "de";
		String url = BASE_URL + "data/2.5/weather?q=" + city + "," + countryCode;
		if (API_KEY != null && API_KEY.isEmpty() == false) {
			url += "&APPID=" + API_KEY;
		}
		String json = util.call(url);
		ObjectMapper mapper = new ObjectMapper();
		try {
			CurrentData data = mapper.readValue(json, CurrentData.class);
			data.getSys().setSunrise(data.getSys().getSunrise() * 1000l);
			data.getSys().setSunset(data.getSys().getSunset() * 1000l);
			data.setDt(data.getDt() * 1000l);
			return data;
		} catch (Exception e) {
			OpenWeatherMapApplication.instance.logger.error("irradiation could not be calculated");
			return null;
		}
	}

}
