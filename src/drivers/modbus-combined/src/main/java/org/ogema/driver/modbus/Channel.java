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
package org.ogema.driver.modbus;

import java.io.IOException;

import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;

/**
 * This class manages the driver data for each channel
 * 
 * @author pau
 * 
 */
public abstract class Channel {

	public static final String TYPE_INPUT = "INPUT_REGISTERS";
	public static final String TYPE_HOLDING = "HOLDING_REGISTERS";
	public static final String TYPE_DISCRETE = "DISCRETE_INPUTS";
	public static final String TYPE_COIL = "COILS";

	protected ChannelLocator locator;

	/**
	 * Channel Factory
	 * 
	 * @param locator
	 * @return
	 */
	public static Channel createChannel(ChannelLocator locator)
			throws IllegalArgumentException {
		String[] splitAddress = locator.getChannelAddress().split(":");

		switch (splitAddress[1]) {
		case TYPE_INPUT:
			return new InputChannel(locator, splitAddress);
		case TYPE_HOLDING:
			return new HoldingChannel(locator, splitAddress);
		case TYPE_DISCRETE:
			return new DiscreteChannel(locator, splitAddress);
		case TYPE_COIL:
			return new CoilChannel(locator, splitAddress);
		default:
			throw new IllegalArgumentException(
					"could not create Channel with Address "
							+ locator.getChannelAddress());
		}
	}

	protected Channel(ChannelLocator locator) {
		this.locator = locator;
	}

	public ChannelLocator getChannelLocator() {
		return locator;
	}

	abstract public SampledValue readValue(Connection connection)
			throws IOException;

	abstract public void writeValue(Connection connection, Value value)
			throws IOException;

}
