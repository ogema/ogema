/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
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
	private final byte[] emptyMessagePayload = new byte[0];

	public CommandChannel(ChannelLocator locator, String[] splitAddress, Device dev) {
		super(locator);
		this.setDevice(dev);
		byte[] commandIdArray = DatatypeConverter.parseHexBinary(splitAddress[1]);
		byte commandId = commandIdArray[0];
		deviceCommand = dev.getRemoteDevice().deviceCommands.get(commandId);
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
		byte[] messagePayload = value.getByteArrayValue();
		if (messagePayload == null)
			messagePayload = emptyMessagePayload;
		deviceCommand.performCommand(messagePayload);
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
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeUpdateListener() throws IOException, UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

}
