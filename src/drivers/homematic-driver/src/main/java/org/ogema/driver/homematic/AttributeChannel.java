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
package org.ogema.driver.homematic;

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
import org.ogema.driver.homematic.manager.DeviceAttribute;
import org.slf4j.Logger;

public final class AttributeChannel extends Channel {

	private DeviceAttribute deviceAttribute;
	private final byte[] emptyMessagePayload = new byte[0];
	private SampledValueContainer sampledValueContainer;
	private ChannelUpdateListener channelEventListener;
	private List<SampledValueContainer> sampledValueContainerList = new ArrayList<SampledValueContainer>();
	private ByteBuffer messagePayloadBuffer;
	private Map<Short, SampledValue> sampledValueMap = new HashMap<Short, SampledValue>();
	private final Logger logger = org.slf4j.LoggerFactory.getLogger("homematic-driver");
	private ArrayList<DeviceAttribute> deviceAttributes = new ArrayList<DeviceAttribute>();
	private final boolean multipleAttributes;

	public AttributeChannel(ChannelLocator locator, String[] splitAddress, Device dev) {
		super(locator);
		if (splitAddress.length <= 2) { // Only one attribute
			multipleAttributes = false;
			byte[] attributeIdArray = DatatypeConverter.parseHexBinary(splitAddress[1]);
			short attributeId = (short) ((short) (attributeIdArray[0] << 8) & 0xff00);
			attributeId |= attributeIdArray[1] & 0x00ff;
			deviceAttribute = dev.getRemoteDevice().deviceAttributes.get(attributeId);
		}
		else { // Multiple attributes
			multipleAttributes = true;
			byte[] messagePayload = new byte[(splitAddress.length - 3) * 2]; // Store all attributeIds starting from the
			// second, *2 because each id consists
			// of 2 bytes
			messagePayloadBuffer = ByteBuffer.wrap(messagePayload);

			for (int i = 1; i < splitAddress.length; ++i) {
				byte[] attributeIdArray = DatatypeConverter.parseHexBinary(splitAddress[i]);
				short attributeId = (short) ((short) (attributeIdArray[0] << 8) & 0xff00);
				attributeId |= attributeIdArray[1] & 0x00ff;
				if (i > 1) // Skip the first attribute ID because it is implied
					// when sending via that attribute
					messagePayloadBuffer.putShort(Short.reverseBytes(attributeId));
				logger.info(" " + Integer.toHexString(attributeId));
				deviceAttributes.add(dev.getRemoteDevice().deviceAttributes.get(attributeId));
			}
		}
	}

	@Override
	public SampledValue readValue(Connection connection) throws IOException, UnsupportedOperationException {
		if (multipleAttributes) {
			for (DeviceAttribute deviceAttribute : deviceAttributes) { // Retrieve the values from all attributes
				sampledValueMap.put(deviceAttribute.getIdentifier(), new SampledValue(deviceAttribute.getValue(),
						System.currentTimeMillis(), Quality.GOOD));
			}
			Value value = new ObjectValue(sampledValueMap);
			return new SampledValue(value, System.currentTimeMillis(), Quality.GOOD); // TODO use average for Quality?
		}
		else {
			return new SampledValue(deviceAttribute.getValue(), System.currentTimeMillis(), Quality.GOOD);
		}
	}

	/**
	 * The value has to be a ByteArrayValue in Little Endian Byte order
	 */
	@Override
	public void writeValue(Connection connection, Value value) throws IOException, UnsupportedOperationException {
		// NYI!
		if (multipleAttributes) {
			throw new UnsupportedOperationException(); // TODO implement this method
		}
		else {
			if (deviceAttribute.readOnly() || !(value instanceof ByteArrayValue))
				throw new UnsupportedOperationException();
			byte[] messagePayload = value.getByteArrayValue();
			if (messagePayload == null)
				messagePayload = emptyMessagePayload;
		}
	}

	@Override
	public void setEventListener(SampledValueContainer container, ChannelUpdateListener listener) throws IOException,
			UnsupportedOperationException {
		if (multipleAttributes) {
			throw new UnsupportedOperationException(); // TODO implement this method
		}
		else {
			deviceAttribute.setChannel(this);
			deviceAttribute.setListener(true);
			sampledValueContainer = container;
			channelEventListener = listener;
			sampledValueContainerList.add(container); // TODO the whole solution with a list for one container is
			// ugly...
		}
	}

	@Override
	public void removeUpdateListener() {
		if (multipleAttributes) {
			throw new UnsupportedOperationException(); // TODO implement this method
		}
		else {
			deviceAttribute.setListener(false);
			channelEventListener = null;
			sampledValueContainerList.clear();
			sampledValueContainer = null;
		}
	}

	public void updateListener() {
		// TODO adjust quality
		if (multipleAttributes) {
			throw new UnsupportedOperationException(); // TODO implement this method
		}
		else {
			sampledValueContainer.setSampledValue(new SampledValue(deviceAttribute.getValue(), System
					.currentTimeMillis(), Quality.GOOD));
			channelEventListener.channelsUpdated(sampledValueContainerList);
		}
	}
}
