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
package org.ogema.hardwaremanager.rpi;

import java.util.HashMap;
import java.util.Map;

import org.ogema.hardwaremanager.api.Container;
import org.ogema.hardwaremanager.api.NativeAccess;

public class NativeAccessImpl implements NativeAccess {

	private static final String LIBRARY = "ogemahwmanager";

	public NativeAccessImpl() {
		System.loadLibrary(LIBRARY);

		initialize();
	}

	/**
	 * called at startup to prepare the native side
	 */
	public native void initialize();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ogema.core.hardwaremanager.impl.NativeAccess#getHandles()
	 */
	@Override
	public native Container[] getHandles();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ogema.core.hardwaremanager.impl.NativeAccess#getEvent(org.ogema. hardwaremanager.impl.Container)
	 */
	@Override
	public native Container getEvent(Container container);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ogema.core.hardwaremanager.impl.NativeAccess#unblock()
	 */
	@Override
	public native void unblock();

	@Override
	public native String getIdString(Object handle);

	@Override
	public native String getPortString(Object handle);

	public native Map<String, String> getNativeUsbInfo(Object handle, Map<String, String> map);

	@Override
	public Map<String, String> getUsbInfo(Object handle) {
		Map<String, String> result = new HashMap<String, String>();
		return getNativeUsbInfo(handle, result);
	}
}
