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
package org.ogema.core.channelmanager.driverspi;

import org.ogema.core.channelmanager.measurements.Value;

/**
 * A Container for a Value and a ChannelLocator,
 * 
 */
public class ValueContainer {

	private Value value;
	private final ChannelLocator channelLocator;

	/**
	 * Constructor for ValueContainer
	 * 
	 * @param channelLocator
	 * @param value
	 */
	public ValueContainer(ChannelLocator channelLocator, Value value) {
		super();
		this.value = value;
		this.channelLocator = channelLocator;
	}

	/**
	 * 
	 * @return {@link ChannelLocator}
	 */
	public ChannelLocator getChannelLocator() {
		return channelLocator;
	}

	/**
	 * 
	 * @return {@link Value}
	 */
	public Value getValue() {
		return value;
	}

	/**
	 * 
	 * @param value
	 */
	public void setValue(Value value) {
		this.value = value;
	}
}
