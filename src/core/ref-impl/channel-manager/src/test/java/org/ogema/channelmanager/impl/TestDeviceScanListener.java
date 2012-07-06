/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ogema.channelmanager.impl;

import java.util.LinkedList;
import java.util.List;

import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.channelmanager.driverspi.DeviceScanListener;

class TestDeviceScanListener implements DeviceScanListener {

	public float ratio = 0.f;
	public boolean success = false;
	public boolean finished = false;
	public Exception finishedException = null;
	public List<DeviceLocator> foundDevices = null;

	@Override
	public void deviceFound(DeviceLocator device) {
		if (foundDevices == null) {
			foundDevices = new LinkedList<DeviceLocator>();
		}

		foundDevices.add(device);
	}

	@Override
	public void finished(boolean success, Exception e) {
		finished = true;
		this.success = success;
		this.finishedException = e;
	}

	@Override
	public void progress(float ratio) {
		this.ratio = ratio;
	}

}
