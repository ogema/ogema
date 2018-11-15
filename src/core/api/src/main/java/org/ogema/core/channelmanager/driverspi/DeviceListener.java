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
package org.ogema.core.channelmanager.driverspi;

/**
 * Monitor available Devices in an asynchronous way
 */
public interface DeviceListener {
	/**
	 * Callback method that signals the detection of a new device by the low level driver.
	 * 
	 * @param device
	 *            The DeviceLocator object that describes the found device uniquely.
	 */
	public void deviceAdded(DeviceLocator device);

	/**
	 * Callback method that signals that a device is no longer reachable.
	 * 
	 * @param device
	 *            The DeviceLocator object that describes the removed device uniquely.
	 */
	public void deviceRemoved(DeviceLocator device);
}
