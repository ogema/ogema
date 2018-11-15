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

import java.util.ArrayList;
import java.util.List;

import org.ogema.core.hardwaremanager.HardwareDescriptor;
import org.ogema.core.hardwaremanager.HardwareListener;
import org.ogema.hardwaremanager.api.NativeAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link HardwareDescriptor}
 */
public class HardwareDescriptorImpl implements HardwareDescriptor {

	/**
	 * Logger instance
	 */
	final static Logger logger = LoggerFactory.getLogger(HardwareDescriptorImpl.class);

	/**
	 * opaque handle to identify the hardware for {@link NativeAccess}
	 */
	private final Object handle;

	/**
	 * The type of descriptor
	 */
	private final HardwareType type;

	/**
	 * The identifier string for the Hardware
	 */
	private final String identifier;

	/**
	 * Listeners to be informed, when this descriptor becomes unavailable.
	 */
	private final List<HardwareListener> listeners;

	/**
	 * set to false, when the Descriptor is about to be removed (during callback)
	 */
	private boolean alive;

	/**
	 * static factory method for {@link HardwareDescriptorImpl}
	 * 
	 * @param nativeAccess
	 *            Interface to system-dependent methods
	 * @param handle
	 *            opaque handle for {@link NativeAccess}
	 * @param type
	 *            hardware type
	 * @return new instance
	 */
	static HardwareDescriptorImpl newInstance(NativeAccess nativeAccess, Object handle, HardwareType type) {
		HardwareDescriptorImpl descriptor;

		switch (type) {
		case USB:
			descriptor = UsbHardwareDescriptorImpl.newInstance(nativeAccess, handle);
			break;
		case SERIAL:
			descriptor = SerialHardwareDescriptorImpl.newInstance(nativeAccess, handle);
			break;
		case GPIO:
			descriptor = GpioHardwareDescriptorImpl.newInstance(nativeAccess, handle);
			break;
		default:
			throw new IllegalArgumentException("Invalid HardwareType " + type);
		}
		return descriptor;
	}

	/**
	 * Protected constructor called by the extended factory methods.
	 * 
	 * @param type
	 *            hardware type
	 * @param handle
	 *            opaque handle
	 * @param identifier
	 *            id string
	 */
	HardwareDescriptorImpl(HardwareType type, Object handle, String identifier) {
		this.type = type;
		this.handle = handle;
		this.identifier = identifier;
		this.alive = true;
		listeners = new ArrayList<HardwareListener>();
	}

	/**
	 * The HardwareManager informs the descriptor with this callback that the hardware it represents has been
	 * disconnected. Calls in turn all its registered listeners.
	 */
	void disconnected() {
		// ArrayList<HardwareListener> arrayList;
		alive = false;

		// make a copy of the listeners list to avoid a
		// ConcurrentModificationException
		// synchronized (listeners) {
		// arrayList = new ArrayList<>(listeners);
		// }

		for (HardwareListener listener : listeners) {
			try {
				listener.hardwareRemoved(this);
			} catch (Throwable t) {
				logger.warn("A HardwareListener threw an exception", t);
			}
		}
	}

	public Object getHandle() {
		return handle;
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public HardwareType getHardwareType() {
		return type;
	}

	@Override
	public void addListener(HardwareListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	@Override
	public void removeListener(HardwareListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	@Override
	public boolean isAlive() {
		return alive;
	}

	@Override
	public String toString() {
		return identifier;
	}

}
