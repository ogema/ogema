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
package org.ogema.channelmapper.impl;

public class MappedElement {
	private String attributePath = null;
	private ChannelDescription channelDescription = null;
	private String defaultValue = null;

	public MappedElement() {

	}

	public MappedElement(String attributePath, ChannelDescription channelDescription) {
		this.attributePath = attributePath;
		this.channelDescription = channelDescription;
	}

	public MappedElement(String attributePath, String defaultValue) {
		this.attributePath = attributePath;
		this.defaultValue = defaultValue;
	}

	public void setAttributePath(String attributePath) {
		this.attributePath = attributePath;
	}

	public void setChannelDescription(ChannelDescription channelDescription) {
		this.channelDescription = channelDescription;
	}

	public String getAttributePath() {
		return attributePath;
	}

	public ChannelDescription getChannelDescription() {
		return channelDescription;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

}
