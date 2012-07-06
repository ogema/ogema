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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.ChannelUpdateListener;
import org.ogema.core.channelmanager.driverspi.SampledValueContainer;
import org.ogema.core.channelmanager.measurements.ByteArrayValue;
import org.ogema.core.channelmanager.measurements.ObjectValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.driver.xbee.manager.zcl.ClusterAttribute;
import org.slf4j.Logger;

public final class AttributeChannel extends Channel {

	private ClusterAttribute clusterAttribute;
	private final byte[] emptyMessagePayload = new byte[0];
	private final Object channelLock = new Object();
	private SampledValueContainer sampledValueContainer;
	private ChannelUpdateListener channelUpdateListener;
	private List<SampledValueContainer> sampledValueContainerList = new ArrayList<SampledValueContainer>();
	private ByteBuffer messagePayloadBuffer;
	private Map<Short, SampledValue> sampledValueMap = new HashMap<Short, SampledValue>();
	private final Logger logger = org.slf4j.LoggerFactory.getLogger("xbee-driver");
	private ArrayList<ClusterAttribute> clusterAttributes = new ArrayList<ClusterAttribute>();
	private final boolean multipleAttributes;

	public AttributeChannel(ChannelLocator locator, String[] splitAddress, Device dev) {
		super(locator);
		byte[] clusterIdArray = DatatypeConverter.parseHexBinary(splitAddress[0]);
		short clusterId = (short) ((short) (clusterIdArray[0] << 8) & 0xff00);
		clusterId |= clusterIdArray[1] & 0x00ff;

		if (splitAddress.length <= 3) { // Only one attribute
			multipleAttributes = false;
			byte[] attributeIdArray = DatatypeConverter.parseHexBinary(splitAddress[2]);
			short attributeId = (short) ((short) (attributeIdArray[0] << 8) & 0xff00);
			attributeId |= attributeIdArray[1] & 0x00ff;
			clusterAttribute = dev.getEndpoint().getClusters().get(clusterId).clusterAttributes.get(attributeId);
			device = dev;
		}
		else { // Multiple attributes
			multipleAttributes = true;
			byte[] messagePayload = new byte[(splitAddress.length - 3) * 2]; // Store
			// all
			// attributeIds
			// starting
			// from
			// the
			// second, *2 because each id consists of 2 bytes
			messagePayloadBuffer = ByteBuffer.wrap(messagePayload);

			for (int i = 2; i < splitAddress.length; ++i) {
				byte[] attributeIdArray = DatatypeConverter.parseHexBinary(splitAddress[i]);
				short attributeId = (short) ((short) (attributeIdArray[0] << 8) & 0xff00);
				attributeId |= attributeIdArray[1] & 0x00ff;
				if (i > 2) // Skip the first attribute ID because it is implied
					// when sending via that attribute
					messagePayloadBuffer.putShort(Short.reverseBytes(attributeId));
				logger.info(" " + Integer.toHexString(attributeId));
				clusterAttributes
						.add(dev.getEndpoint().getClusters().get(clusterId).clusterAttributes.get(attributeId));
			}
			device = dev;
		}
	}

	@Override
	public SampledValue readValue(Connection connection) throws IOException, UnsupportedOperationException {
		if (multipleAttributes) {
			// Use the first attribute with the ids of the other attributes as
			// payload
			connection.localDevice.sendRequestMessage(clusterAttributes.get(0).getMessage((byte) 0x00,
					messagePayloadBuffer.array()), Constants.READ_ATTRIBUTES_RESPONSE, channelLock);
			synchronized (channelLock) {
				try {
					channelLock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			for (ClusterAttribute clusterAttribute : clusterAttributes) { // Retrieve
				// the
				// values
				// from
				// all
				// attributes
				if (Constants.calendar.getTimeInMillis() - clusterAttribute.getValueTimestamp() > (connection.localDevice
						.getCyclicSleepPeriod() + 2000)) {
					sampledValueMap.put(clusterAttribute.getIdentifier(), new SampledValue(clusterAttribute.getValue(),
							System.currentTimeMillis(), Quality.BAD));
				}
				else {
					sampledValueMap.put(clusterAttribute.getIdentifier(), new SampledValue(clusterAttribute.getValue(),
							System.currentTimeMillis(), Quality.GOOD));
				}
			}
			Value value = new ObjectValue(sampledValueMap);
			SampledValue sv = new SampledValue(value, System.currentTimeMillis(), Quality.GOOD); // TODO use average for
			// Quality?
			return sv;

		}
		else {
			connection.localDevice.sendRequestMessage(clusterAttribute.getMessage((byte) 0x00, emptyMessagePayload),
					Constants.READ_ATTRIBUTES_RESPONSE, channelLock);
			synchronized (channelLock) {
				try {
					channelLock.wait(60000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (Constants.calendar.getTimeInMillis() - clusterAttribute.getValueTimestamp() > (connection.localDevice
					.getCyclicSleepPeriod() + 2000)) {
				SampledValue sv = new SampledValue(clusterAttribute.getValue(), System.currentTimeMillis(), Quality.BAD);
				return sv;
			}
			else {
				SampledValue sv = new SampledValue(clusterAttribute.getValue(), System.currentTimeMillis(),
						Quality.GOOD);
				return sv;
			}
		}
	}

	/**
	 * The value has to be a ByteArrayValue in Little Endian Byte order
	 */
	@Override
	public void writeValue(Connection connection, Value value) throws IOException, UnsupportedOperationException {
		if (multipleAttributes) {
			throw new UnsupportedOperationException(); // TODO implement this
			// method
		}
		else {
			if (clusterAttribute.readOnly() || !(value instanceof ByteArrayValue))
				throw new UnsupportedOperationException();
			byte[] messagePayload = value.getByteArrayValue();
			if (messagePayload == null)
				messagePayload = emptyMessagePayload;
			connection.localDevice.sendRequestMessage(clusterAttribute.getMessage((byte) 0x02, messagePayload),
					Constants.WRITE_ATTRIBUTES_RESPONSE, channelLock);
			synchronized (channelLock) {
				try {
					channelLock.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void setUpdateListener(SampledValueContainer container, ChannelUpdateListener listener) throws IOException,
			UnsupportedOperationException {
		if (multipleAttributes) {
			throw new UnsupportedOperationException(); // TODO implement this
			// method
		}
		else {
			clusterAttribute.setChannel(this);
			clusterAttribute.setListener(true);
			sampledValueContainer = container;
			channelUpdateListener = listener;
			sampledValueContainerList.add(container); // TODO the whole solution
			// with a list for one
			// container is ugly...
		}
	}

	@Override
	public void removeUpdateListener() {
		if (multipleAttributes) {
			throw new UnsupportedOperationException(); // TODO implement this
			// method
		}
		else {
			clusterAttribute.setListener(false);
			channelUpdateListener = null;
			sampledValueContainerList.clear();
			sampledValueContainer = null;
		}
	}

	public void updateListener() {
		// TODO adjust quality
		if (multipleAttributes) {
			throw new UnsupportedOperationException(); // TODO implement this
			// method
		}
		else {
			sampledValueContainer.setSampledValue(new SampledValue(clusterAttribute.getValue(), System
					.currentTimeMillis(), Quality.GOOD));
			channelUpdateListener.channelsUpdated(sampledValueContainerList);
		}
	}
}
