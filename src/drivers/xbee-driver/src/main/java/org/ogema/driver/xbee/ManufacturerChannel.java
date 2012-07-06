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
package org.ogema.driver.xbee;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.ChannelUpdateListener;
import org.ogema.core.channelmanager.driverspi.SampledValueContainer;
import org.ogema.core.channelmanager.measurements.ByteArrayValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.driver.xbee.manager.zcl.Cluster;
import org.ogema.driver.xbee.manager.zcl.ClusterAttribute;
import org.ogema.driver.xbee.manager.zcl.ClusterCommand;

public class ManufacturerChannel extends Channel {

	private short manufacturerId;
	private short attributeId;
	private byte commandId;
	private Cluster cluster;
	private ClusterAttribute clusterAttribute;
	@SuppressWarnings("unused")
	private ClusterCommand clusterCommand;
	private final byte[] emptyMessagePayload = new byte[0];
	private final Object channelLock = new Object();
	private SampledValueContainer sampledValueContainer;
	private ChannelUpdateListener channelUpdateListener;
	private List<SampledValueContainer> sampledValueContainerList = new ArrayList<SampledValueContainer>();
	private boolean isAttribute;

	protected ManufacturerChannel(ChannelLocator locator, String[] splitAddress, Device dev) {
		super(locator);
		this.setDevice(dev);

		// Parse Cluster ID
		byte[] clusterIdArray = DatatypeConverter.parseHexBinary(splitAddress[0]);
		short clusterId = (short) ((short) (clusterIdArray[0] << 8) & 0xff00);
		clusterId |= clusterIdArray[1] & 0x00ff;

		// Parse type
		switch (splitAddress[2]) {
		case "COMMAND":
			isAttribute = false;
			break;
		case "ATTRIBUTE":
			isAttribute = true;
			break;
		}

		if (isAttribute) {
			// Parse Attribute ID
			byte[] attributeIdArray = DatatypeConverter.parseHexBinary(splitAddress[3]);
			attributeId = (short) ((short) (attributeIdArray[0] << 8) & 0xff00);
			attributeId |= attributeIdArray[1] & 0x00ff;
			cluster = dev.getEndpoint().getClusters().get(clusterId);
			if (!cluster.clusterAttributes.containsKey(attributeId)) {
				cluster.clusterAttributes.put(attributeId, new ClusterAttribute(cluster, attributeId, "", false, true));
			}

			clusterAttribute = cluster.clusterAttributes.get(attributeId);
		}
		else {
			byte[] commandIdArray = DatatypeConverter.parseHexBinary(splitAddress[3]);
			byte commandId = commandIdArray[0];
			cluster = dev.getEndpoint().getClusters().get(clusterId);
			if (!cluster.clusterCommands.containsKey(attributeId)) {
				cluster.clusterCommands.put(commandId, new ClusterCommand(cluster, commandId, "", true));
			}

			clusterCommand = cluster.clusterCommands.get(attributeId);
		}

		// Parse Manufacturer ID
		byte[] manufacturerIdArray = DatatypeConverter.parseHexBinary(splitAddress[4]);
		if (manufacturerIdArray.length == 0) { // load from NodeDescriptor if empty string
			manufacturerId = dev.getEndpoint().getDevice().getNodeDescriptor().getManufacturerCode();
		}
		else {
			manufacturerId = (short) ((short) (manufacturerIdArray[0] << 8) & 0xff00);
			manufacturerId |= manufacturerIdArray[1] & 0x00ff;
		}
	}

	@Override
	public SampledValue readValue(Connection connection) throws IOException, UnsupportedOperationException {
		if (isAttribute) {
			byte[] message = cluster.getManufacturerSpecificAttributeMessage((byte) 0x00, manufacturerId, attributeId,
					emptyMessagePayload);

			connection.localDevice.sendRequestMessage(message, Constants.READ_ATTRIBUTES_RESPONSE, channelLock);
			synchronized (channelLock) {
				try {
					channelLock.wait(60000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (Constants.calendar.getTimeInMillis() - clusterAttribute.getValueTimestamp() > (connection.localDevice
					.getCyclicSleepPeriod() + 2000)) {
				return new SampledValue(clusterAttribute.getValue(), System.currentTimeMillis(), Quality.BAD);
			}
			else {
				return new SampledValue(clusterAttribute.getValue(), System.currentTimeMillis(), Quality.GOOD);
			}
		}
		else {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public void writeValue(Connection connection, Value value) throws IOException, UnsupportedOperationException {
		if (isAttribute) {
			if (clusterAttribute.readOnly() || !(value instanceof ByteArrayValue))
				throw new UnsupportedOperationException();
			byte[] messagePayload = value.getByteArrayValue();
			if (messagePayload == null)
				messagePayload = emptyMessagePayload;
			connection.localDevice.sendRequestMessage(cluster.getManufacturerSpecificAttributeMessage((byte) 0x02,
					manufacturerId, attributeId, messagePayload), Constants.WRITE_ATTRIBUTES_RESPONSE, channelLock);
			synchronized (channelLock) {
				try {
					channelLock.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		else {
			byte[] messagePayload = value.getByteArrayValue();
			if (messagePayload == null)
				messagePayload = emptyMessagePayload;
			connection.localDevice.sendMessage(cluster.getManufacturerSpecificCommandMessage(manufacturerId, commandId,
					messagePayload));
		}
	}

	@Override
	public void setUpdateListener(SampledValueContainer container, ChannelUpdateListener listener) throws IOException,
			UnsupportedOperationException {
		if (isAttribute) {
			clusterAttribute.setChannel(this);
			clusterAttribute.setListener(true);
			sampledValueContainer = container;
			channelUpdateListener = listener;
			sampledValueContainerList.add(container); // TODO the whole solution
			// with a list for one
			// container is ugly...
		}
		else {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public void removeUpdateListener() throws IOException, UnsupportedOperationException {
		if (isAttribute) {

			clusterAttribute.setListener(false);
			channelUpdateListener = null;
			sampledValueContainerList.clear();
			sampledValueContainer = null;
		}
		else {
			throw new UnsupportedOperationException();
		}
	}

	public void updateListener() {
		if (isAttribute) {
			sampledValueContainer.setSampledValue(new SampledValue(clusterAttribute.getValue(), System
					.currentTimeMillis(), Quality.GOOD));
			channelUpdateListener.channelsUpdated(sampledValueContainerList);
		}
		else {
			throw new UnsupportedOperationException();
		}
	}

}
