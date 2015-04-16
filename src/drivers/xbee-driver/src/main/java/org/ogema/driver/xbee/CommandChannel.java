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
package org.ogema.driver.xbee;

import java.io.IOException;

import javax.xml.bind.DatatypeConverter;

import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.ChannelUpdateListener;
import org.ogema.core.channelmanager.driverspi.SampledValueContainer;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.driver.xbee.manager.zcl.ClusterCommand;

public class CommandChannel extends Channel {

	private ClusterCommand clusterCommand;
	private final byte[] emptyMessagePayload = new byte[0];

	public CommandChannel(ChannelLocator locator, String[] splitAddress, Device dev) {
		super(locator);
		this.setDevice(dev);
		byte[] clusterIdArray = DatatypeConverter.parseHexBinary(splitAddress[0]);
		short clusterId = (short) ((short) (clusterIdArray[0] << 8) & 0xff00);
		clusterId |= clusterIdArray[1] & 0x00ff;
		byte[] attributeIdArray = DatatypeConverter.parseHexBinary(splitAddress[2]);
		byte attributeId = attributeIdArray[0];
		clusterCommand = dev.getEndpoint().getClusters().get(clusterId).clusterCommands.get(attributeId);
		clusterCommand.getIdentifier();
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
		connection.localDevice.sendMessage(clusterCommand.getMessage(messagePayload));
	}

	@Override
	public void setUpdateListener(SampledValueContainer container, ChannelUpdateListener listener) throws IOException,
			UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeUpdateListener() throws IOException, UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

}
