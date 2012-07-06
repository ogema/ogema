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

import org.ogema.core.hardwaremanager.SerialHardwareDescriptor;
import org.ogema.hardwaremanager.api.NativeAccess;

public class SerialHardwareDescriptorImpl extends HardwareDescriptorImpl implements SerialHardwareDescriptor {

	private final String portName;

	static SerialHardwareDescriptorImpl newInstance(NativeAccess nativeAccess, Object handle) {

		String identifier = nativeAccess.getIdString(handle);
		String portName = nativeAccess.getPortString(handle);

		return new SerialHardwareDescriptorImpl(handle, identifier, portName);
	}

	private SerialHardwareDescriptorImpl(Object handle, String identifier, String portName) {
		super(HardwareType.SERIAL, handle, identifier);
		this.portName = portName;
	}

	@Override
	public String getPortName() {
		return portName;
	}

}
