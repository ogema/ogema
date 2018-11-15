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
package org.ogema.driver.homematic.manager;

import java.util.Calendar;

import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.driver.homematic.AttributeChannel;
import org.slf4j.Logger;

/**
 * 
 * @author puschas/baerthbn
 * 
 */
public class DeviceAttribute {
	private final short identifier;
	private final String attributeName;
	private final String channelAddress;
	private final boolean readOnly;
	private Value value;
	private long valueTimestamp;
	private final Logger logger = org.slf4j.LoggerFactory.getLogger("homematic-driver");
	private AttributeChannel attributeChannel;
	private boolean haslistener = false;
	private Calendar calendar;

	public DeviceAttribute(short identifier, String attributeName, boolean readOnly, boolean mandatory) {
		this.calendar = Calendar.getInstance();
		this.identifier = identifier;
		this.attributeName = attributeName;
		this.readOnly = readOnly;
		channelAddress = generateChannelAddress();
		logger.debug("Channel Address: " + channelAddress);
	}

	/**
	 * unused as of now. Creates the channel address of the attribute.
	 * 
	 * @return
	 */
	private String generateChannelAddress() {
		StringBuilder tempString = new StringBuilder();
		tempString.append("Attribute:");
		tempString.append(Integer.toHexString(identifier & 0xffff));
		switch (tempString.length()) {
		case 10:
			tempString.append("0000");
			break;
		case 11:
			tempString.insert(tempString.length() - 1, "000");
			break;
		case 12:
			tempString.insert(tempString.length() - 2, "00");
			break;
		case 13:
			tempString.insert(tempString.length() - 3, "0");
			break;
		}
		return tempString.toString();
	}

	public short getIdentifier() {
		return identifier;
	}

	public String getChannelAddress() {
		return channelAddress;
	}

	public boolean readOnly() {
		return readOnly;
	}

	public String getAttributeName() {
		return attributeName;
	}

	public Value getValue() {
		return value;
	}

	public void setValue(Value value) {
		this.value = value;
		valueTimestamp = calendar.getTimeInMillis();
		if (haslistener) {
			attributeChannel.updateListener();
		}
	}

	public long getValueTimestamp() {
		return valueTimestamp;
	}

	public void setChannel(AttributeChannel attributeChannel) {
		this.attributeChannel = attributeChannel;
	}

	public void setListener(boolean b) {
		haslistener = b;
	}

	/**
	 * @throws UnsupportedOperationException
	 */
	public void unsupportedAttribute() {
		throw new UnsupportedOperationException();
	}
}
