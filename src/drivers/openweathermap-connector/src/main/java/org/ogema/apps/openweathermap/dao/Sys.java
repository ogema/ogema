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

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "type", "id", "message", "country", "sunrise", "sunset" })
public class Sys {

	@JsonProperty("type")
	private Long type;
	@JsonProperty("id")
	private Long id;
	@JsonProperty("message")
	private Double message;
	@JsonProperty("country")
	private String country;
	@JsonProperty("sunrise")
	private Long sunrise;
	@JsonProperty("sunset")
	private Long sunset;
	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	@JsonProperty("type")
	public Long getType() {
		return type;
	}

	@JsonProperty("type")
	public void setType(Long type) {
		this.type = type;
	}

	@JsonProperty("id")
	public Long getId() {
		return id;
	}

	@JsonProperty("id")
	public void setId(Long id) {
		this.id = id;
	}

	@JsonProperty("message")
	public Double getMessage() {
		return message;
	}

	@JsonProperty("message")
	public void setMessage(Double message) {
		this.message = message;
	}

	@JsonProperty("country")
	public String getCountry() {
		return country;
	}

	@JsonProperty("country")
	public void setCountry(String country) {
		this.country = country;
	}

	@JsonProperty("sunrise")
	public Long getSunrise() {
		return sunrise;
	}

	@JsonProperty("sunrise")
	public void setSunrise(Long sunrise) {
		this.sunrise = sunrise;
	}

	@JsonProperty("sunset")
	public Long getSunset() {
		return sunset;
	}

	@JsonProperty("sunset")
	public void setSunset(Long sunset) {
		this.sunset = sunset;
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
