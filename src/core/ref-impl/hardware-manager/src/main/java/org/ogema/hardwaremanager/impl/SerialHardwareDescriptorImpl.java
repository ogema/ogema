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
