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
