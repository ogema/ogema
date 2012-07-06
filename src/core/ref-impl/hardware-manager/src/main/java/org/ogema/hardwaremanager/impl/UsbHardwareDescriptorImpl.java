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

import java.util.Collections;
import java.util.Map;

import org.ogema.core.hardwaremanager.UsbHardwareDescriptor;
import org.ogema.hardwaremanager.api.NativeAccess;

public class UsbHardwareDescriptorImpl extends HardwareDescriptorImpl implements UsbHardwareDescriptor {
	private final String portName;
	private final Map<String, String> info;

	static HardwareDescriptorImpl newInstance(NativeAccess nativeAccess, Object handle) {
		String identifier = nativeAccess.getIdString(handle);
		String portName = nativeAccess.getPortString(handle);
		Map<String, String> info = Collections.unmodifiableMap(nativeAccess.getUsbInfo(handle));

		return new UsbHardwareDescriptorImpl(handle, identifier, portName, info);
	}

	private UsbHardwareDescriptorImpl(Object handle, String identifier, String portName, Map<String, String> info) {
		super(HardwareType.USB, handle, identifier);
		this.portName = portName;
		this.info = info;
	}

	@Override
	public String getPortName() {
		return portName;
	}

	@Override
	public Map<String, String> getInfo() {
		return info;
	}
}
