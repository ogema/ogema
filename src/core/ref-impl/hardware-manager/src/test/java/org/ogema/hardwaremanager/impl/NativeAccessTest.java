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
package org.ogema.hardwaremanager.impl;

import java.util.HashMap;
import java.util.Map;

import org.ogema.core.hardwaremanager.HardwareDescriptor;
import org.ogema.hardwaremanager.api.Container;
import org.ogema.hardwaremanager.api.NativeAccess;

public class NativeAccessTest implements NativeAccess {

	public Map<Object, NativeDescriptor> devices = new HashMap<Object, NativeDescriptor>();

	private int toContainerType(HardwareDescriptor.HardwareType type) {
		switch (type) {
		case USB:
			return Container.TYPE_USB;
		case SERIAL:
			return Container.TYPE_SERIAL;
		case GPIO:
			return Container.TYPE_GPIO;
		default:
			throw new IllegalArgumentException(type.toString());
		}
	}

	@Override
	public Container[] getHandles() {
		Container[] result = null;
		int i = 0;

		for (NativeDescriptor descriptor : devices.values()) {
			if (null == result)
				result = new Container[devices.values().size()];
			result[i++] = new Container(descriptor, toContainerType(descriptor.type), Container.EVENT_NONE);
		}
		return result;
	}

	@Override
	public Container getEvent(Container container) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void unblock() {
		// TODO Auto-generated method stub

	}

	public void addHandle(NativeDescriptor descriptor) {
		devices.put(descriptor, descriptor);
	}

	@Override
	public String getIdString(Object handle) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPortString(Object handle) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, String> getUsbInfo(Object handle) {
		// TODO Auto-generated method stub
		return null;
	}

}
