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

import java.util.Map;

import org.ogema.hardwaremanager.api.Container;
import org.ogema.hardwaremanager.api.NativeAccess;

/**
 * No-op implementation of {@link NativeAccess}. Used when no {@link NativeAccess} service was found by the Activator.
 */
public class DummyNativeAccess implements NativeAccess {

	private volatile boolean exit;

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
//			e.printStackTrace();
			Thread.currentThread().interrupt();
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
