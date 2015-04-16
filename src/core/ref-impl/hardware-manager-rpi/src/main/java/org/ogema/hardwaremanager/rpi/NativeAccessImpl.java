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
