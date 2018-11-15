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

import javax.xml.bind.DatatypeConverter;

import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.ChannelUpdateListener;
import org.ogema.core.channelmanager.driverspi.SampledValueContainer;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.driver.homematic.manager.DeviceCommand;

public class CommandChannel extends Channel {

	private DeviceCommand deviceCommand;
	private Device device;

	// private final byte[] emptyMessagePayload = new byte[0];

	public CommandChannel(ChannelLocator locator, String[] splitAddress, Device dev) {
		super(locator);
		this.setDevice(dev);
		byte[] commandIdArray = DatatypeConverter.parseHexBinary(splitAddress[1]);
		byte commandId = commandIdArray[0];
		deviceCommand = dev.getRemoteDevice().getSubDevice().deviceCommands.get(commandId);
		deviceCommand.getIdentifier();
	}

	@Override
	public SampledValue readValue(Connection connection) throws IOException, UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	/**
	 * The value has to be a ByteArrayValue in reversed byte order
	 */
	@Override
	public void writeValue(Connection connection, Value value) throws IOException {
		// byte[] messagePayload = value.getByteArrayValue();
		// if (messagePayload == null)
		// messagePayload = emptyMessagePayload;
		deviceCommand.channelChanged(value);
	}

	public Device getDevice() {
		return device;
	}

	public void setDevice(Device device) {
		this.device = device;
	}

	@Override
	public void setEventListener(SampledValueContainer container, ChannelUpdateListener listener) throws IOException,
			UnsupportedOperationException {
		// throw new UnsupportedOperationException();
	}

	@Override
	public void removeUpdateListener() throws IOException, UnsupportedOperationException {
		// throw new UnsupportedOperationException();
	}

}
