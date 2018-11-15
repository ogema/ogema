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
