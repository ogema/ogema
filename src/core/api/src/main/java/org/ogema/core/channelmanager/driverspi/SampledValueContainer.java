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

import org.ogema.core.channelmanager.measurements.SampledValue;

/**
 * 
 * A Container for SampledValues. The SampeldValueContainer contains a {@link SampledValue} and a {@link ChannelLocator}
 * .
 * 
 * 
 */
public class SampledValueContainer {
	private SampledValue sampledValue = null;
	private ChannelLocator channelLocator;

	/**
	 * Constructor
	 * 
	 * @param channelLocator
	 */
	public SampledValueContainer(ChannelLocator channelLocator) {
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
	 * @return {@link SampledValue}
	 */
	public SampledValue getSampledValue() {
		return sampledValue;
	}

	/**
	 * 
	 * @param value
	 */
	public void setSampledValue(SampledValue value) {
		this.sampledValue = value;
	}
}
