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
package org.ogema.driver.homematic;

import java.io.IOException;

import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.ChannelUpdateListener;
import org.ogema.core.channelmanager.driverspi.SampledValueContainer;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;

/**
 * Each channel represents a Homematic Command/Attribute
 * 
 * @author puschas,baerthbn
 * 
 */
abstract class Channel {
	protected ChannelLocator locator;

	protected Channel(ChannelLocator locator) {
		this.locator = locator;
	}

	public static Channel createChannel(ChannelLocator locator, Device dev) {
		String[] splitAddress = locator.getChannelAddress().split(":");
		switch (splitAddress[0]) {
		case "COMMAND":
			return new CommandChannel(locator, splitAddress, dev);
		case "ATTRIBUTE":
			return new AttributeChannel(locator, splitAddress, dev);
		default:
			break;
		}
		throw new NullPointerException("could not create Channel with Address " + locator.getChannelAddress());
	}

	public ChannelLocator getChannelLocator() {
		return locator;
	}

	abstract public SampledValue readValue(Connection connection) throws IOException, UnsupportedOperationException;

	abstract public void writeValue(Connection connection, Value value) throws IOException,
			UnsupportedOperationException;

	abstract public void setEventListener(SampledValueContainer container, ChannelUpdateListener listener)
			throws IOException, UnsupportedOperationException;

	abstract public void removeUpdateListener() throws IOException, UnsupportedOperationException;
}
