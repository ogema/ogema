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
package org.ogema.channels;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.ogema.core.application.AppID;
import org.ogema.core.channelmanager.ChannelAccessException;
import org.ogema.core.channelmanager.driverspi.DeviceListener;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.slf4j.Logger;

class DeviceListenerList {
	
	final private Driver driver;
	final private Logger logger;
	
	/** set of registered device listeners */
	private final Set<DeviceListenerImpl> deviceListeners = new HashSet<DeviceListenerImpl>();
	
	DeviceListenerList(Driver driver, Logger logger) {
		this.driver = driver;
		this.logger = logger;
	}
	
	synchronized void addDeviceListener(AppID appId, DeviceListener listener) throws ChannelAccessException {

		DeviceListenerImpl deviceListener = new DeviceListenerImpl(listener, appId);
		
		// if the set accepts the element, add it to the driver
		if (deviceListeners.add(deviceListener)) {
			driver.getDriver().addDeviceListener(deviceListener);
		}
	}

	// should not throw an exception. Application clean-up code might not be very robust.
	synchronized void removeDeviceListener(AppID appId, DeviceListener listener) {

		DeviceListenerImpl deviceListener = new DeviceListenerImpl(listener, appId);
		
		// remove will return true if the set contained the element
		if (deviceListeners.remove(deviceListener)) {
			driver.getDriver().removeDeviceListener(deviceListener);
		}
	}		

	synchronized void removeDeviceListenersForApp(AppID appID) {
		
		Iterator<DeviceListenerImpl> iter = deviceListeners.iterator();
		
		while(iter.hasNext()) {
			DeviceListenerImpl current = iter.next();
			
			if (current.appId.equals(appID)) {
				iter.remove();
				driver.getDriver().removeDeviceListener(current);
			}
		}
	}
	
	synchronized void close() {
		for (DeviceListenerImpl deviceListener : deviceListeners) {
			driver.getDriver().removeDeviceListener(deviceListener);
		}
		
		deviceListeners.clear();	
	}
	
	/**
	 * Helper class to decouple driver callbacks from the application code.
	 * 
	 * @author pau
	 *
	 */
	private class DeviceListenerImpl implements DeviceListener {

		final private DeviceListener listener;
		final private AppID appId;
		
		DeviceListenerImpl(DeviceListener listener, AppID appId) {
			
			if (listener == null)
				throw new NullPointerException();
			
			if (appId == null)
				throw new NullPointerException();
			
			this.listener = listener;
			this.appId = appId;
		}
		
		@Override
		public void deviceAdded(DeviceLocator device) {
			try {
				listener.deviceAdded(device);
			} catch (Throwable t) {
				logger.warn("caught application exception in DeviceListener callback", t);
			}
		}

		@Override
		public void deviceRemoved(DeviceLocator device) {
			try {
				listener.deviceRemoved(device);
			} catch (Throwable t) {
				logger.warn("caught application exception in DeviceListener callback", t);
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((appId == null) ? 0 : appId.hashCode());
			result = prime * result + ((listener == null) ? 0 : listener.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DeviceListenerImpl other = (DeviceListenerImpl) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (appId == null) {
				if (other.appId != null)
					return false;
			} else if (!appId.equals(other.appId))
				return false;
			if (listener == null) {
				if (other.listener != null)
					return false;
			} else if (!listener.equals(other.listener))
				return false;
			return true;
		}

		private DeviceListenerList getOuterType() {
			return DeviceListenerList.this;
		}
	}
}