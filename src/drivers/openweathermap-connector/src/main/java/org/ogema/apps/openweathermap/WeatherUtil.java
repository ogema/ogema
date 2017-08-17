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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

//import org.joda.time.DateTime;
import org.ogema.apps.openweathermap.dao.Clouds;
import org.ogema.apps.openweathermap.dao.CurrentData;
import org.ogema.apps.openweathermap.dao.ForecastData;
import org.ogema.apps.openweathermap.dao.Main;
import org.ogema.apps.openweathermap.dao.OpenWeatherMapREST;
import org.ogema.apps.openweathermap.dao.Rain;

/**
 * 
 * Access to the openweathermap services.
 * 
 * @author brequardt
 */
public class WeatherUtil {

	private final TrustManager[] trustAllCerts;
	private static WeatherUtil instance;
	private boolean ssl = false;
	private final SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");

	private WeatherUtil() {

		javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(new javax.net.ssl.HostnameVerifier() {
			@Override
			public boolean verify(String hostname, javax.net.ssl.SSLSession sslSession) {
				return true;
			}
		});
		trustAllCerts = new TrustManager[] { new X509TrustManager() {
			@Override
			public void checkClientTrusted(final X509Certificate[] chain, final String authType) {
			}

			@Override
			public void checkServerTrusted(final X509Certificate[] chain, final String authType) {
			}

			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}
		} };
	}

	public String call(String querry) {

		if (ssl) {
			return callSSL(querry);
		}

		String result = "{}";
		try {
			final URLConnection urlCon = new URL(querry).openConnection();
			urlCon.setRequestProperty("accept", "text/json");
			final InputStream input = urlCon.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(urlCon.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				result = line;
			}
			input.close();

		} catch (IOException e) {
			OpenWeatherMapApplication.instance.appMan.getLogger().error(
					"Exception by http-Request to OpenWeathermap: " + e.getMessage());
			return "{}";
		}
		return result;
	}

	private String callSSL(String querry) {

		String result = "{}";
		try {
			final SSLContext sslContext = SSLContext.getInstance("SSL");
			sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
			final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
			final URLConnection urlCon = new URL(querry).openConnection();
			urlCon.setRequestProperty("accept", "text/json");
			((HttpsURLConnection) urlCon).setSSLSocketFactory(sslSocketFactory);
			final InputStream input = urlCon.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(urlCon.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				result = line;
			}
			input.close();

		} catch (NoSuchAlgorithmException | KeyManagementException | IOException e) {
			return "ERROR: " + e.getStackTrace();
		}
		return result;
	}

	public boolean ping(String ip, Integer port, Integer timeout) {

		try {
			Socket so = new Socket();
			so.connect(new InetSocketAddress(ip, port), timeout);
			so.close();
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	public static WeatherUtil getInstance() {
		if (WeatherUtil.instance == null) {
			WeatherUtil.instance = new WeatherUtil();
		}

		return WeatherUtil.instance;
	}

	public boolean isSsl() {
		return ssl;
	}

	public void setSsl(boolean ssl) {
		this.ssl = ssl;
	}

	public ForecastData interpolateForecast(ForecastData data, int intervallInMinutes) {

		org.ogema.apps.openweathermap.dao.List first, last;
		Collections.sort(data.getList());
		List<org.ogema.apps.openweathermap.dao.List> resultList = new ArrayList<>();
		final int intervall = intervallInMinutes * 60;
		for (int i = 0; i < data.getList().size() - 1; i++) {
			first = data.getList().get(i);
			last = data.getList().get(i + 1);
			resultList.addAll(interpolate(first, last, intervall));
		}
		if (resultList.size() > 0) {
			Collections.sort(resultList);
			data.setList(resultList);
			calculateIrradiation(data);
		}
		return data;
	}

	private List<org.ogema.apps.openweathermap.dao.List> interpolate(org.ogema.apps.openweathermap.dao.List first,
			org.ogema.apps.openweathermap.dao.List last, int intervall) {

		List<org.ogema.apps.openweathermap.dao.List> result = new ArrayList<>();
		final Integer COUNT = Long.valueOf((last.getDt() - first.getDt()) / intervall).intValue();

		// Clouds
		List<Double> clouds = split(first.getClouds().getAll().doubleValue(), last.getClouds().getAll().doubleValue(),
				COUNT);

		// Main
		List<Double> grndLevel = split(first.getMain().getGrndLevel(), last.getMain().getGrndLevel(), COUNT);
		List<Double> humidity = split(first.getMain().getHumidity().doubleValue(), last.getMain().getHumidity()
				.doubleValue(), COUNT);
		List<Double> pressure = split(first.getMain().getPressure(), last.getMain().getPressure(), COUNT);
		List<Double> seaLevel = split(first.getMain().getSeaLevel(), last.getMain().getSeaLevel(), COUNT);
		List<Double> temp = split(first.getMain().getTemp(), last.getMain().getTemp(), COUNT);
		List<Double> tempMax = split(first.getMain().getTempMax(), last.getMain().getTempMax(), COUNT);
		List<Double> tempMin = split(first.getMain().getTempMin(), last.getMain().getTempMin(), COUNT);

		// Rain
		List<Double> rainData;
		if (first.getRain() != null && last.getRain() != null) {
			Double rain1 = (double) first.getRain().get3h() / COUNT;
			Double rain2 = (double) last.getRain().get3h() / COUNT;
			rainData = split(rain1, rain2, COUNT);
			;
		}
		else {
			rainData = split(0.0d, 0.0d, COUNT);
		}

		for (Integer i = 0; i < COUNT; i++) {
			org.ogema.apps.openweathermap.dao.List entry = new org.ogema.apps.openweathermap.dao.List();
			// Clouds
			Clouds cloud = new Clouds(clouds.get(i).intValue());
			entry.setClouds(cloud);
			entry.getAdditionalProperties().putAll(first.getAdditionalProperties());

			// Rain
			Rain rain = new Rain();
			rain.set3h(rainData.get(i).intValue());
			entry.setRain(rain);

			// Main-Weather
			Main main = new Main();
			main.setGrndLevel(grndLevel.get(i));
			main.setHumidity(humidity.get(i).intValue());
			main.setPressure(pressure.get(i));
			main.setTemp(temp.get(i));
			main.setTempMax(tempMax.get(i));
			main.setTempMin(tempMin.get(i));
			main.setSeaLevel(seaLevel.get(i));
			entry.setMain(main);

			// DateTime //"2015-03-17 12:00:00"
			long millis = (first.getDt() + intervall * i) * 1000;
			millis += TimeZone.getDefault().getOffset(millis);
			entry.setDt(millis);
			String txt = sdf.format(new Date(millis));
			entry.setDtTxt(txt);

			entry.setSys(first.getSys());
			result.add(entry);
		}

		Collections.sort(result);
		return result;
	}

	private List<Double> split(final Double value1, final Double value2, final Integer COUNT) {
		final List<Double> result = new ArrayList<>();
		Double diff = Math.abs(Math.abs(value1) - Math.abs(value2));

		if (value1 >= value2) {
			diff *= -1;
		}
		final Double STEP = diff / COUNT;
		Double value = value1;
		for (Integer i = 0; i < COUNT; i++) {
			result.add(value);
			value += STEP;
		}
		return result;
	}

	public String toString(ForecastData data) {
		final StringBuilder sb = new StringBuilder();
		String city = data.getCity().getName();
		for (org.ogema.apps.openweathermap.dao.List entry : data.getList()) {
			String date = entry.getDtTxt();
			String cloud = entry.getClouds().getAll() + "";
			String humidity = entry.getMain().getHumidity() + "";
			Double irradiance = (Math.round((entry.getIrradiation()) * 100) / 100.0);
			String tab = "\t";
			if (irradiance > 10000) {
				tab = "";
			}

			Double temp = (Math.round((entry.getMain().getTemp() - 273.15) * 100) / 100.0);
			sb.append(city).append(" (").append(date).append("): ").append(cloud).append(" % Wolken,\t")
					.append(irradiance).append(" Watt/mÂ²\t").append(tab).append(humidity).append("% Feuchtigkeit,\t")
					.append(temp).append(" Â°C\n");
		}

		return sb.toString();
	}

	public String toString(CurrentData data) {
		final StringBuilder sb = new StringBuilder();
		String city = data.getName();
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		String sunUp = sdf.format(new Date(data.getSys().getSunrise()));
		String sunDown = sdf.format(new Date(data.getSys().getSunset()));
		Double temp = (Math.round((data.getMain().getTemp() - 273.15) * 100) / 100.0);

		sb.append(city).append(":\n\tSonnenaufgang: ").append(sunUp).append(", Sonnenuntergang: ").append(sunDown)
				.append("\n\t").append("Temperatur: ").append(temp).append(", Luftdruck: ")
				.append(data.getMain().getPressure()).append("\n\t").append("Wind: ").append(data.getWind().getSpeed())
				.append("km/h");

		return sb.toString();
	}

	private void calculateIrradiation(ForecastData data) {
		final String city = data.getCity().getName();
		final String country = data.getCity().getCountry();

		final CurrentData current = OpenWeatherMapREST.getInstance().getWeatherCurrent(city, country);
		if (current != null) {
			final long sunUp = getMillisOfDay(current.getSys().getSunrise());
			final long sunDown = getMillisOfDay(current.getSys().getSunset());

			final Double latidute = data.getCity().getCoord().getLat(); // Breitengrad
			final Double longitude = data.getCity().getCoord().getLon(); // LÃ¤ngengrad

			for (final org.ogema.apps.openweathermap.dao.List entry : data.getList()) {
				Integer cloudPercent = entry.getClouds().getAll();

				long millis = entry.getDt();

				// TODO: Sonnenauf- und untergang
				Double irradiance = 0.0d;
				long dayMillis = getMillisOfDay(millis);
				if (sunUp < dayMillis && dayMillis < sunDown) {
					irradiance = calculateIrradiation(latidute, longitude, cloudPercent, millis);
				}
				entry.setIrradiation(irradiance);
			}
		}
		else {
			OpenWeatherMapApplication.instance.logger.error("current weather not received sunrise is missing");
		}
	}

	private long getMillisOfDay(long millis) {
		long day = 24 * 60 * 60 * 1000l;
		return millis % day;
	}

	private Double calculateIrradiation(final Double latidute, final Double longitude, final Integer cloudPercent,
			final long LT) {

//		DateTime cal = new DateTime(LT);
		Calendar cal = Calendar.getInstance(Locale.ENGLISH);
		cal.setTimeInMillis(LT);

		final Double LSTM = 15.0d;
		final Double EoT = getEoT(LT);
		final Double TC = 4 * (longitude - LSTM) + EoT;
//		final Double LST = (double) cal.getHourOfDay() + (double) cal.getMinuteOfHour() / 60 + (TC / 60);
		final Double LST = (double) cal.get(Calendar.HOUR_OF_DAY) + (double) cal.get(Calendar.MINUTE) / 60 + (TC / 60);

		final Double HRA = (-1 * LSTM) * (LST - 12);
		final Double B = getB(LT);

		// So far, all angles were expressed as gradiant values. However, they
		// need to be converted to Radians because Math functions use those!
		final Double deklination = 23.45 * Math.sin(Math.toRadians(B));
		final Double angleSunEarth = Math.asin(Math.cos(Math.toRadians(latidute))
				* Math.cos(Math.toRadians(deklination)) * Math.cos(Math.toRadians(HRA))
				+ Math.sin(Math.toRadians(latidute)) * Math.sin(Math.toRadians(deklination)));

		Double irradiance = (1066 - cloudPercent * 3.14d) * Math.sin(angleSunEarth);
		if (irradiance < 0) {
			irradiance = 0.0d;
		}

		return irradiance;
	}

	private Double getEoT(long millis) {
		final Double B = getB(millis);
		final Double eot = 9.87f * Math.sin(2 * B) - 7.53 * Math.cos(B) - 1.5 * Math.sin(B);
		return eot;
	}

	private Double getB(long millis) {
		final Integer d = getDayofYear(millis);
		Double B = (double) 360 / 365 * (double) d - 81;
		return B;
	}

	private Integer getDayofYear(long millis) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(millis);
		return cal.get(Calendar.DAY_OF_YEAR);

	}

}
