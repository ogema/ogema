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
@JsonPropertyOrder({ "temp", "temp_min", "temp_max", "pressure", "sea_level", "grnd_level", "humidity" })
public class Main {

	@JsonProperty("temp")
	private Double temp;
	@JsonProperty("temp_min")
	private Double tempMin;
	@JsonProperty("temp_max")
	private Double tempMax;
	@JsonProperty("pressure")
	private Double pressure;
	@JsonProperty("sea_level")
	private Double seaLevel;
	@JsonProperty("grnd_level")
	private Double grndLevel;
	@JsonProperty("humidity")
	private Integer humidity;
	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	@JsonProperty("temp")
	public Double getTemp() {
		return temp;
	}

	@JsonProperty("temp")
	public void setTemp(Double temp) {
		this.temp = temp;
	}

	@JsonProperty("temp_min")
	public Double getTempMin() {
		return tempMin;
	}

	@JsonProperty("temp_min")
	public void setTempMin(Double tempMin) {
		this.tempMin = tempMin;
	}

	@JsonProperty("temp_max")
	public Double getTempMax() {
		return tempMax;
	}

	@JsonProperty("temp_max")
	public void setTempMax(Double tempMax) {
		this.tempMax = tempMax;
	}

	@JsonProperty("pressure")
	public Double getPressure() {
		return pressure;
	}

	@JsonProperty("pressure")
	public void setPressure(Double pressure) {
		this.pressure = pressure;
	}

	@JsonProperty("sea_level")
	public Double getSeaLevel() {
		return seaLevel;
	}

	@JsonProperty("sea_level")
	public void setSeaLevel(Double seaLevel) {
		this.seaLevel = seaLevel;
	}

	@JsonProperty("grnd_level")
	public Double getGrndLevel() {
		return grndLevel;
	}

	@JsonProperty("grnd_level")
	public void setGrndLevel(Double grndLevel) {
		this.grndLevel = grndLevel;
	}

	@JsonProperty("humidity")
	public Integer getHumidity() {
		return humidity;
	}

	@JsonProperty("humidity")
	public void setHumidity(Integer humidity) {
		this.humidity = humidity;
	}

	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	@JsonAnySetter
	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}

}
