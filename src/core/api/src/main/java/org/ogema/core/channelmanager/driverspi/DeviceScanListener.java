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
package org.ogema.core.channelmanager.driverspi;

/**
 * Monitor available Devices in synchronous way
 */
public interface DeviceScanListener {
	/**
	 * 
	 * @param device
	 */
	public void deviceFound(DeviceLocator device);

	/**
	 * 
	 * @param success
	 * @param e
	 */
	public void finished(boolean success, Exception e);

	/** 0.0:not started, 1.0:finished */
	public void progress(float ratio);
}
