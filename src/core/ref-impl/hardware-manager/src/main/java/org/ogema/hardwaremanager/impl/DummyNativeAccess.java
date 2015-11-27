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
package org.ogema.hardwaremanager.impl;

import java.util.Map;

import org.ogema.hardwaremanager.api.Container;
import org.ogema.hardwaremanager.api.NativeAccess;

/**
 * No-op implementation of {@link NativeAccess}. Used when no {@link NativeAccess} service was found by the Activator.
 */
public class DummyNativeAccess implements NativeAccess {

	private boolean exit;

	@Override
	public Container[] getHandles() {
		return new Container[0];
	}

	@Override
	public Container getEvent(Container container) {

		try {
			while (!exit) {
				synchronized (this) {
					wait();
				}
			}
			exit = false;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return container;
	}

	@Override
	public String getIdString(Object handle) {
		return null;
	}

	@Override
	public String getPortString(Object handle) {
		return null;
	}

	@Override
	public Map<String, String> getUsbInfo(Object handle) {
		return null;
	}

	@Override
	public void unblock() {
		synchronized (this) {
			exit = true;
			notifyAll();
		}
	}

}
