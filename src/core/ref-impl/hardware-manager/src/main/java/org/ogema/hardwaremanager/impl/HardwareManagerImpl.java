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

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.ReferencePolicyOption;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.hardwaremanager.HardwareDescriptor;
import org.ogema.core.hardwaremanager.HardwareListener;
import org.ogema.core.hardwaremanager.HardwareManager;
import org.ogema.core.hardwaremanager.UsbHardwareDescriptor;
import org.ogema.hardwaremanager.api.Container;
import org.ogema.hardwaremanager.api.NativeAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate=true)
@Service(HardwareManager.class)
// TODO issue listener callbacks in separate threads -> avoid blocking the main thread
public class HardwareManagerImpl implements HardwareManager, Runnable {
	
	/**
	 * Do not access this directly, use {@link #getNativeAccess()} instead, which
	 * never returns null.
	 */
	@Reference(cardinality=ReferenceCardinality.OPTIONAL_UNARY, policy=ReferencePolicy.STATIC, policyOption=ReferencePolicyOption.GREEDY)
	private volatile NativeAccess nativeAccess;

	/**
	 * Logger instance
	 */
	private final static Logger logger = LoggerFactory.getLogger(HardwareManagerImpl.class);;

	/**
	 * Event thread
	 */
	private volatile Thread thread;

	/**
	 * Map unique id strings to the HardwareDescriptors
	 */
	private final Map<String, HardwareDescriptorImpl> devices = new HashMap<>();

	private final Collection<HardwareListener> listeners= Collections.synchronizedSet(new HashSet<HardwareListener>());
	
	public HardwareManagerImpl() {
	}
	
	// just for the tests
	HardwareManagerImpl(NativeAccess nativeAccess) {
		this.nativeAccess = nativeAccess;
		activate();
	}

	@Activate
	public void activate() {

		final NativeAccess nativeAccess = getNativeAccess();
		// get initial connected hardware
		final Container[] containers = nativeAccess.getHandles();

		if (null == containers) {
			logger.warn("nativeAccess.getHandles() == null. No initial devices detected.");
		}
		else {
			synchronized (devices) {
				for (Container container : containers) {
					HardwareDescriptorImpl descriptor;
					descriptor = HardwareDescriptorImpl.newInstance(nativeAccess, container.handle, container.getType());
					devices.put(descriptor.getIdentifier(), descriptor);
				}
			}
		}
		logger.debug("Starting event thread.");
		thread = new Thread(this, "OGEMA HardwareManager event thread");
		thread.start();
	}

	@Deactivate
	public void exit() {
		if (thread.isAlive()) {
			thread.interrupt();
		}
		NativeAccess nativeAccess = getNativeAccess();
		nativeAccess.unblock();
		try {
			thread.join(2000);
		} catch (InterruptedException e) {
			logger.warn("thread.join() was interrupted.", e);
			Thread.currentThread().interrupt();
		}
		if (thread.isAlive())
			logger.error("event thread is still alive after timeout.");
		synchronized (devices) {
			devices.clear();
		}
		listeners.clear();
		thread = null;
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
		NativeAccess nativeAccess;
		while (!Thread.interrupted()) {
			nativeAccess = getNativeAccess();
			container = nativeAccess.getEvent(container);
			if (container == null) {
				logger.warn("Got a null container from native access {}",nativeAccess);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				continue;
			}
			switch (container.event) {
			case Container.EVENT_NONE:
				break;
			case Container.EVENT_ADD:
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
				synchronized (devices) {
					Iterator<HardwareDescriptorImpl> it = devices.values().iterator();
					while (it.hasNext()) {
						descriptor = it.next();
						// we found our handle
						if (descriptor.getHandle().equals(container.handle)) {
							it.remove();
							descriptor.disconnected();
							callListenersRemoved(descriptor);
							break;
						}
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
	
	@Override
	public String getPortName(final String portNameProp, String descriptorRegEx, HardwareListener hwListener) {
		String portName = AccessController.doPrivileged(new PrivilegedAction<String>() {

			@Override
			public String run() {
				return System.getProperty(portNameProp);
			}
		});
		if (portName == null) {
			// String hardwareDesriptors = System.getProperty("org.ogema.apps.xyplotter.hardware-descriptor",
			// ".+:1a86:7523:");
			logger.info(String.format(
					"No device file specified on the command line. The Hardware descriptor %s is used instead.",
					descriptorRegEx));
			Collection<HardwareDescriptor> descriptors = getHardwareDescriptors(descriptorRegEx);
			// logger.info(
			// String.format("Portname via hardware descriptor: %s.%s", hardwareDesriptors, descriptors.size()));
			for (HardwareDescriptor descr : descriptors) {
				portName = ((UsbHardwareDescriptor) descr).getPortName();
				if (portName != null) {
					break;
				}
			}
		}
		logger.info(String.format("Port name detected %s", portName));
		if (portName == null && hwListener != null)
			addListener(hwListener);
		return portName;
	}

	
	private final static DummyNativeAccess dummy = new DummyNativeAccess();
	
	/**
	 * Never returns null
	 * @return
	 */
	NativeAccess getNativeAccess() {
		NativeAccess na = nativeAccess;
		if (na == null)
			na = dummy;
		return na;
	}
}
