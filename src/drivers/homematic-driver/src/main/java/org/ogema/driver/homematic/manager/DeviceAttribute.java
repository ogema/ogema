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
