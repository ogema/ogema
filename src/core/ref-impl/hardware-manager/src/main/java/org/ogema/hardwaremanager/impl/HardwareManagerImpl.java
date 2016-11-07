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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Pattern;

import org.ogema.core.hardwaremanager.HardwareDescriptor;
import org.ogema.core.hardwaremanager.HardwareListener;
import org.ogema.core.hardwaremanager.HardwareManager;
import org.ogema.hardwaremanager.api.Container;
import org.ogema.hardwaremanager.api.NativeAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HardwareManagerImpl implements HardwareManager, Runnable {

	/**
	 * Logger instance
	 */
	private final static Logger logger = LoggerFactory.getLogger(HardwareManagerImpl.class);;

	/**
	 * Event thread
	 */
	private final Thread thread;

	/**
	 * Map unique id strings to the HardwareDescriptors
	 */
	private final Map<String, HardwareDescriptorImpl> devices;

	private final Collection<HardwareListener> listeners;

	private final NativeAccess nativeAccess;

	/**
	 * if set, the event thread exits
	 */
	private volatile boolean exit;

	public HardwareManagerImpl(NativeAccess nativeAccess) {
		devices = new HashMap<String, HardwareDescriptorImpl>();
		listeners = Collections.synchronizedSet(new HashSet<HardwareListener>());

		this.nativeAccess = nativeAccess;

		// get initial connected hardware
		Container[] containers = nativeAccess.getHandles();

		if (null == containers) {
			logger.warn("nativeAccess.getHandles() == null. No initial devices detected.");
		}
		else
			for (Container container : containers) {
				HardwareDescriptorImpl descriptor;

				descriptor = HardwareDescriptorImpl.newInstance(nativeAccess, container.handle, container.getType());
				devices.put(descriptor.getIdentifier(), descriptor);
			}

		logger.debug("Starting event thread.");
		thread = new Thread(this, "OGEMA HardwareManager event thread");
		thread.start();
	}

	public void exit() {
		exit = true;
		nativeAccess.unblock();
		try {
			thread.join(2000);
		} catch (InterruptedException e) {
			logger.error("thread.join() was interrupted.", e);
		}
		if (thread.isAlive())
			logger.error("event thread is still alive after timeout.");
	}

	@Override
	public Collection<String> getHardwareIdentifiers() {
		synchronized (devices) {
			return new ArrayList<String>(devices.keySet());
		}
	}

	@Override
	public Collection<HardwareDescriptor> getHardwareDescriptors() {
		synchronized (devices) {
			return new ArrayList<HardwareDescriptor>(devices.values());
		}
	}

	@Override
	public Collection<HardwareDescriptor> getHardwareDescriptors(String pattern) {
		ArrayList<HardwareDescriptor> result = new ArrayList<HardwareDescriptor>();
		synchronized (devices) {
			for (HardwareDescriptor descr : devices.values()) {
				if (Pattern.matches(pattern, descr.getIdentifier()))
					result.add(descr);
			}
			return result;
		}
	}

	@Override
	public HardwareDescriptor getDescriptor(String identifier) {
		synchronized (devices) {
			return devices.get(identifier);
		}
	}

	@Override
	public void addListener(HardwareListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(HardwareListener listener) {
		listeners.remove(listener);
	}

	@Override
	public void run() {
		Container container = new Container();

		while (!exit) {
			container = nativeAccess.getEvent(container);

			switch (container.event) {
			case Container.EVENT_NONE:
				break;
			case Container.EVENT_ADD: {
				HardwareDescriptorImpl descriptor;
				HardwareDescriptorImpl old;

				descriptor = HardwareDescriptorImpl.newInstance(nativeAccess, container.handle, container.getType());

				synchronized (devices) {
					old = devices.put(descriptor.getIdentifier(), descriptor);
				}

				if (old != null) {
					old.disconnected();
					callListenersRemoved(descriptor);
				}

				callListenersAdded(descriptor);
			}
				break;
			case Container.EVENT_REMOVE:
				// the native method returns the native handle.
				// to remove the hardware descriptor you need the id string.
				// But we can't query the native side for the id string because
				// the hardware is already disconnected.
				// So we need to search the hash map values linearly for the
				// required handle.
				// No need to synchronize here, because devices is only
				// modified by the event thread itself, and searching does not
				// modify devices.
				for (HardwareDescriptorImpl descriptor : devices.values()) {
					// we found our handle
					if (descriptor.getHandle().equals(container.handle)) {
						synchronized (devices) {
							devices.remove(descriptor.getIdentifier());
						}
						descriptor.disconnected();
						callListenersRemoved(descriptor);
						break;
					}
				}
				break;
			default:
				logger.warn("unkown event {} from NativeAccess.getEvent()", container.event);
				break;
			}
		}
		logger.debug("exiting event thread.");
	}

	private void callListenersAdded(HardwareDescriptorImpl descriptor) {

		ArrayList<HardwareListener> arrayList;

		logger.debug("added descriptor {}", descriptor.getIdentifier());

		// make a copy of the listeners list to avoid a
		// ConcurrentModificationException
		arrayList = new ArrayList<>(listeners);

		for (HardwareListener listener : arrayList) {
			try {
				listener.hardwareAdded(descriptor);
			} catch (Throwable t) {
				logger.warn("A HardwareListener threw an Exception.", t);
			}
		}
	}

	private void callListenersRemoved(HardwareDescriptorImpl descriptor) {

		ArrayList<HardwareListener> arrayList;

		logger.debug("removed descriptor {}", descriptor.getIdentifier());

		// make a copy of the listeners list to avoid a
		// ConcurrentModificationException
		arrayList = new ArrayList<>(listeners);

		for (HardwareListener listener : arrayList) {
			try {
				listener.hardwareRemoved(descriptor);
			} catch (Throwable t) {
				logger.warn("A HardwareListener threw an Exception.", t);
			}
		}
	}
}
