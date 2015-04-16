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
package org.ogema.app.DRS485DEconsole;

import java.util.ArrayList;
import java.util.List;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceDemandListener;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.driver.DRS485DE.DRS485DEConfigurationModel;
import org.ogema.model.metering.ElectricityMeter;

/**
 * This class holds and manages the list of all configurations. At startup it scans for persistent configurations. It
 * also registers a listener for newly created configurations.
 * 
 * @author pau
 * 
 */
public class Manager implements ResourceDemandListener<DRS485DEConfigurationModel> {

	private List<Device> devices = new ArrayList<Device>();
	private ApplicationManager appManager;
	private Listener listener;

	/** private listener class to match the ElectricityMeter resources with the configuration resources */
	private class Listener implements ResourceDemandListener<ElectricityMeter> {

		@Override
		public void resourceAvailable(ElectricityMeter resource) {
			String deviceName;
			String resourceName = resource.getName();

			for (Device device : devices) {
				deviceName = device.getResourceName();

				if (resourceName.equals(deviceName)) {
					device.setDataResource(resource);
					break;
				}
			}
		}

		@Override
		public void resourceUnavailable(ElectricityMeter resource) {
			for (Device device : devices) {
				if (resource.getName().equals(device.getResourceName())) {
					device.setDataResource(null);
				}
			}
		}
	}

	public Manager(ApplicationManager appManager) {
		this.appManager = appManager;
		this.listener = new Listener();

		appManager.getResourceAccess().addResourceDemand(DRS485DEConfigurationModel.class, this);
		appManager.getResourceAccess().addResourceDemand(ElectricityMeter.class, listener);

		List<DRS485DEConfigurationModel> list = appManager.getResourceAccess().getResources(
				DRS485DEConfigurationModel.class);

		for (DRS485DEConfigurationModel model : list) {
			resourceAvailable(model);
		}

	}

	public void stop() {
		appManager.getResourceAccess().removeResourceDemand(DRS485DEConfigurationModel.class, this);
		appManager.getResourceAccess().addResourceDemand(ElectricityMeter.class, listener);

		// for(Device device : devices) {
		// if (device != null) {
		// device.close();
		// }
		// }
	}

	private int getFreeIndex() {
		int index;

		for (index = 0; index < devices.size(); index++) {
			if (devices.get(index) == null)
				break;
		}

		return index;
	}

	private int getFreeNameIndex() {
		int index = 0;
		ResourceAccess resources = appManager.getResourceAccess();
		ResourceManagement resMan = appManager.getResourceManagement();

		while (resources.getResource(resMan.getUniqueResourceName("DRS485DEConfiguration_" + index)) != null) {
			index++;
		}

		return index;
	}

	private int containsConfiguration(DRS485DEConfigurationModel resource) {
		for (int index = 0; index < devices.size(); index++) {
			Device device = devices.get(index);

			if (device != null && device.getConfigurationModel().equals(resource))
				return index;
		}
		return -1;
	}

	@Override
	public void resourceAvailable(DRS485DEConfigurationModel resource) {

		// check if already there
		if (containsConfiguration(resource) < 0) {
			Device device = new Device(appManager, resource);

			add(device);
		}
	}

	/** add existing device */
	private int add(Device device) {
		int index = getFreeIndex();

		// if there is a free entry in the list set element
		if (index < devices.size())
			devices.set(index, device);
		// otherwise add to the end of the list
		else
			devices.add(device);

		return index;
	}

	@Override
	public void resourceUnavailable(DRS485DEConfigurationModel resource) {

		Device device;
		int index = containsConfiguration(resource);

		if (index >= 0) {
			device = devices.set(index, null);
			device.delete();
		}
	}

	public List<Device> getDevices() {
		return devices;
	}

	/** create new device with default values */
	public int newDevice() {
		return add(new Device(appManager, getFreeNameIndex()));
	}

	/** get Device at index */
	public Device getDevice(int index) {
		return devices.get(index);
	}

	/**
	 * remove Device at index
	 * 
	 * @param index
	 * @return the removed Device
	 */
	public Device removeDevice(int index) {
		return devices.set(index, null);
	}
}
