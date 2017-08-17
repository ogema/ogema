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
