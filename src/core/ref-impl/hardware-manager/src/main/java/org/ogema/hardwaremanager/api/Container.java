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
package org.ogema.hardwaremanager.api;

import org.ogema.core.hardwaremanager.HardwareDescriptor.HardwareType;

public class Container {

	public static final int EVENT_NONE = 0;
	public static final int EVENT_ADD = 1;
	public static final int EVENT_REMOVE = 2;

	public static final int TYPE_USB = 1;
	public static final int TYPE_SERIAL = 2;
	public static final int TYPE_GPIO = 3;

	public Object handle;
	public int type;
	public int event;

	public Container() {

	}

	public Container(Object handle, int type, int event) {
		this.handle = handle;
		this.type = type;
		this.event = event;
	}

	public HardwareType getType() {
		switch (type) {
		case TYPE_USB:
			return HardwareType.USB;
		case TYPE_GPIO:
			return HardwareType.GPIO;
		case TYPE_SERIAL:
			return HardwareType.SERIAL;
		default:
			throw new IllegalArgumentException("Unkown type " + type);
		}
	}

	private String printEvent() {
		switch (event) {
		case EVENT_NONE:
			return "NONE";
		case EVENT_ADD:
			return "ADD";
		case EVENT_REMOVE:
			return "REMOVE";
		default:
			return "!unknown";
		}
	}

	public String toString() {
		return "type: " + getType() + " event: " + printEvent() + " handle: " + (String) handle;
	}
}
