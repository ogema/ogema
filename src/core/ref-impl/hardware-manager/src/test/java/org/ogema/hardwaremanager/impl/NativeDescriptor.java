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
package org.ogema.hardwaremanager.impl;

import java.util.HashMap;
import java.util.Map;

import org.ogema.core.hardwaremanager.HardwareDescriptor.HardwareType;

public class NativeDescriptor {
	public String identifier;
	public HardwareType type;
	public String port;
	public Map<String, String> usbInfo;

	public NativeDescriptor() {
		usbInfo = new HashMap<String, String>();
	}

	public NativeDescriptor(String identifer, HardwareType type, String port, Map<String, String> usbInfo) {
		this.identifier = identifer;
		this.type = type;
		this.port = port;
		if (usbInfo == null)
			usbInfo = new HashMap<String, String>();
		this.usbInfo = usbInfo;
	}
}
