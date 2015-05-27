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

import java.util.HashMap;
import java.util.Map;

import org.ogema.core.channelmanager.measurements.Value;

public abstract class SubDevice {

	protected RemoteDevice remoteDevice;

	public Map<Byte, DeviceCommand> deviceCommands;
	public Map<Short, DeviceAttribute> deviceAttributes;

	public SubDevice(RemoteDevice rd) {
		this.remoteDevice = rd;

		deviceCommands = new HashMap<Byte, DeviceCommand>();
		deviceAttributes = new HashMap<Short, DeviceAttribute>();
	}

	protected abstract void addMandatoryChannels();

	public abstract void parseValue(StatusMessage msg);

	public abstract void channelChanged(byte identifier, Value value);
}
