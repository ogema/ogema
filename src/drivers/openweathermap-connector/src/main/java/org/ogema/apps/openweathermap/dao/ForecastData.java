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

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "cod", "message", "city", "cnt", "list" })
public class ForecastData {

	@JsonProperty("cod")
	private String cod;
	@JsonProperty("message")
	private float message;
	@JsonProperty("city")
	private City city;
	@JsonProperty("cnt")
	private int cnt;
	@JsonProperty("list")
	private java.util.List<org.ogema.apps.openweathermap.dao.List> list = new ArrayList<>();
	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<>();

	@JsonProperty("cod")
	public String getCod() {
		return cod;
	}

	@JsonProperty("cod")
	public void setCod(String cod) {
		this.cod = cod;
	}

	@JsonProperty("message")
	public float getMessage() {
		return message;
	}

	@JsonProperty("message")
	public void setMessage(float message) {
		this.message = message;
	}

	@JsonProperty("city")
	public City getCity() {
		return city;
	}

	@JsonProperty("city")
	public void setCity(City city) {
		this.city = city;
	}

	@JsonProperty("cnt")
	public int getCnt() {
		return cnt;
	}

	@JsonProperty("cnt")
	public void setCnt(int cnt) {
		this.cnt = cnt;
	}

	@JsonProperty("list")
	public java.util.List<org.ogema.apps.openweathermap.dao.List> getList() {
		return list;
	}

	@JsonProperty("list")
	public void setList(java.util.List<org.ogema.apps.openweathermap.dao.List> list) {
		this.list = list;
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
