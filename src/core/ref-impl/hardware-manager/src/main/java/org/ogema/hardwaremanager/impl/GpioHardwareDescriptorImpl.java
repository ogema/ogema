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

import org.ogema.core.hardwaremanager.GpioHardwareDescriptor;
import org.ogema.hardwaremanager.api.NativeAccess;

public class GpioHardwareDescriptorImpl extends HardwareDescriptorImpl implements GpioHardwareDescriptor {

	static GpioHardwareDescriptorImpl newInstance(NativeAccess nativeAccess, Object handle) {
		String identifier = nativeAccess.getIdString(handle);
		return new GpioHardwareDescriptorImpl(handle, identifier);
	}

	private GpioHardwareDescriptorImpl(Object handle, String identifier) {
		super(HardwareType.GPIO, handle, identifier);
	}

	@Override
	public String getPortName() {
		throw new RuntimeException("GPIO support not yet implemented!");
	}
}
