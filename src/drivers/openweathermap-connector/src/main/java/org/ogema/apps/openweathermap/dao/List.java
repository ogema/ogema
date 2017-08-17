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

package org.ogema.apps.openweathermap.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({ "dt", "main", "weather", "clouds", "wind", "rain", "sys", "dt_txt" })
public class List implements Comparable<List> {

	@JsonProperty("dt")
	private long dt;
	@JsonProperty("main")
	private Main main;
	@JsonProperty("weather")
	private java.util.List<Weather> weather = new ArrayList<Weather>();
	@JsonProperty("clouds")
	private Clouds clouds;
	@JsonProperty("wind")
	private Wind wind;
	@JsonProperty("rain")
	private Rain rain;
	@JsonProperty("sys")
	private Sys sys;
	@JsonProperty("irradiation")
	private double irradiation;
	@JsonProperty("dt_txt")
	private String dtTxt;
	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	@JsonProperty("dt")
	public long getDt() {
		return dt;
	}

	@JsonProperty("dt")
	public void setDt(long dt) {
		this.dt = dt;
	}

	@JsonProperty("main")
	public Main getMain() {
		return main;
	}

	@JsonProperty("main")
	public void setMain(Main main) {
		this.main = main;
	}

	@JsonProperty("weather")
	public java.util.List<Weather> getWeather() {
		return weather;
	}

	@JsonProperty("weather")
	public void setWeather(java.util.List<Weather> weather) {
		this.weather = weather;
	}

	@JsonProperty("clouds")
	public Clouds getClouds() {
		return clouds;
	}

	@JsonProperty("clouds")
	public void setClouds(Clouds clouds) {
		this.clouds = clouds;
	}

	@JsonProperty("wind")
	public Wind getWind() {
		return wind;
	}

	@JsonProperty("wind")
	public void setWind(Wind wind) {
		this.wind = wind;
	}

	@JsonProperty("rain")
	public Rain getRain() {
		return rain;
	}

	@JsonProperty("rain")
	public void setRain(Rain rain) {
		this.rain = rain;
	}

	@JsonProperty("sys")
	public Sys getSys() {
		return sys;
	}

	@JsonProperty("sys")
	public void setSys(Sys sys) {
		this.sys = sys;
	}

	@JsonIgnore
	public double getIrradiation() {
		return irradiation;
	}

	@JsonIgnore
	public void setIrradiation(double irradiation) {
		this.irradiation = irradiation;
	}

	@JsonProperty("dt_txt")
	public String getDtTxt() {
		return dtTxt;
	}

	@JsonProperty("dt_txt")
	public void setDtTxt(String dtTxt) {
		this.dtTxt = dtTxt;
	}

	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	@JsonAnySetter
	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}

	@Override
	public int compareTo(List o) {

		if (dt > o.getDt()) {
			return 1;
		}
		else if (dt > o.getDt()) {
			return -1;
		}
		return 0;
	}

	@Override
	public String toString() {
		return "List [irradiation=" + irradiation + "]";
	}

}
