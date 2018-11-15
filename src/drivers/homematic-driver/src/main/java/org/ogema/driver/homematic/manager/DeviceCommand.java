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

import org.ogema.core.channelmanager.measurements.Value;

/**
 * This class represents a command of a HM Device.
 * 
 * @author puschas/baerthbn
 * 
 */
public class DeviceCommand {
	protected final byte identifier;
	private final String channelAddress;
	private final String description;
	private final boolean mandatory;
	protected SubDevice subDevice;

	public DeviceCommand(SubDevice subDevice, byte commandIdentifier, String description, boolean mandatory) {
		this.identifier = commandIdentifier;
		this.description = description;
		this.mandatory = mandatory;
		this.subDevice = subDevice;
		channelAddress = generateChannelAddress();
	}

	/**
	 * unused as of now. Creates the channel address of the attribute.
	 * 
	 * @return
	 */
	private String generateChannelAddress() {
		StringBuilder tempString = new StringBuilder();
		tempString.append("Command:");
		tempString.append(Integer.toHexString(identifier & 0xff));
		switch (tempString.length()) {
		case 8:
			tempString.append("00");
			break;
		case 9:
			tempString.insert(tempString.length() - 1, "0");
			break;
		}
		return tempString.toString();
	}

	public byte getIdentifier() {
		return identifier;
	}

	public String getDescription() {
		return description;
	}

	public boolean isMandatory() {
		return mandatory;
	}

	public String getChannelAddress() {
		return channelAddress;
	}

	public void channelChanged(Value value) {
		this.subDevice.channelChanged(identifier, value);
	}

}
