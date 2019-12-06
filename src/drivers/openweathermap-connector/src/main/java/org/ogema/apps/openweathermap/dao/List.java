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

	/**
	 * Returns the timestamp in seconds since Epoch
	 *
	 * @return      timestamp in seconds since Epoch
	 */
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
