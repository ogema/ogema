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
