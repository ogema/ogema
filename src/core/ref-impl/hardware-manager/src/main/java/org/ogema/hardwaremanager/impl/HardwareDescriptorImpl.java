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
				logger.error("A HardwareListener threw an exception", t);
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
