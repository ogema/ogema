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
