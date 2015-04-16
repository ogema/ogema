/**
 * Copyright 2009 - 2014
 *
 * Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
 *
 * Fraunhofer IIS
 * Fraunhofer ISE
 * Fraunhofer IWES
 *
 * All Rights reserved
 */
/**
 * 
 */
package org.ogema.drivers.lemoneg;

import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.hardwaremanager.HardwareListener;
import org.ogema.core.hardwaremanager.HardwareDescriptor;
import org.ogema.core.hardwaremanager.HardwareListener;
import org.ogema.core.hardwaremanager.HardwareManager;
import org.ogema.core.hardwaremanager.UsbHardwareDescriptor;
import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceDemandListener;
import org.ogema.core.resourcemanager.ResourceException;
import org.ogema.core.resourcemanager.ResourceListener;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.core.resourcemanager.ResourceDemandListener.AccessLossReason;

/**
 * This upper level driver patiently waits for a configuration resource to show up. When one or more show up, it creates
 * a LemonegDevice for it. This in turn tries to connect to the physical device and updates the corresponding
 * LemonegDataModel instance.
 * 
 * 
 * @author pau, bjg
 * 
 */

public class LemonegDriver implements Application, ResourceDemandListener<LemonegConfigurationModel>, HardwareListener {
	private ApplicationManager appManager;
	private ResourceManagement resourceManager;
	private ResourceAccess resourceAccess;
	private LemonegConfigurationModel configResource;
	private String configResourceId;
	private String hwid;
	public Map<String, LemonegBus> buses;

	public LemonegDriver() {
		buses = new HashMap<String, LemonegBus>();
	}

	@Override
	public void start(ApplicationManager appManager) {
		System.out.println("lemoneg-driver started \n");
		this.appManager = appManager;
		resourceManager = appManager.getResourceManagement();
		resourceAccess = appManager.getResourceAccess();
		// register demand for LemonegConfigurationModel
		resourceAccess.addResourceDemand(LemonegConfigurationModel.class, this);

		System.out.println("available hw descriptors:" + appManager.getHardwareManager().getHardwareDescriptors());
		printConnectedHardware(appManager);
		// addBus("usb:1-1.3.4.4:1.0:0403:6001:FTU74ZQE", "modbus-rtu", "115200:8:even:1:none:none:0:500", "7000");
		// addDevice("usb:1-1.3.4.4:1.0:0403:6001:FTU74ZQE", "multi:12:0", "3", "lemoneg1");
	}

	@Override
	public void stop(AppStopReason reason) {
		System.out.println("lemoneg-driver stopped \n");
		appManager.getResourceAccess().removeResourceDemand(LemonegConfigurationModel.class, this);

		for (LemonegBus bus : buses.values()) {
			for (LemonegDevice device : bus.devices.values()) {
				device.close();
			}
		}
	}

	@Override
	public void resourceAvailable(LemonegConfigurationModel lemoneg_conf_model) {
		for (LemonegBus bus : buses.values()) {
			if (bus.interfaceId.equals(lemoneg_conf_model.interfaceId().getValue())) {
				if (!bus.devices.containsKey(lemoneg_conf_model.resourceName().getValue())) {
					LemonegDevice device = new LemonegDevice(appManager, lemoneg_conf_model);
					bus.devices.put(lemoneg_conf_model.resourceName().getValue(), device);
				}
			}
		}
	}

	@Override
	public void resourceUnavailable(LemonegConfigurationModel resource) {
		for (LemonegBus bus : buses.values()) {
			if (bus.interfaceId.equals(resource.interfaceId().getValue())) {
				LemonegDevice device = bus.devices.get(resource.resourceName().getValue());
				if (device != null) {
					bus.devices.remove(resource.resourceName().getValue());
					device.close();
				}
			}
		}
	}

	// create new bus and add it to buses
	public void addBus(String hardwareId, String driverId, String deviceParameters, String timeout) {
		if (!buses.containsKey(hardwareId)) {
			LemonegBus bus = new LemonegBus(hardwareId, driverId, deviceParameters, timeout);
			bus.interfaceId = findHwDescriptor(appManager, hardwareId);
			buses.put(hardwareId, bus);
		}
	}

	public void removeBus(String hardwareId) {
		for (LemonegDevice device : buses.get(hardwareId).devices.values()) {
			device.close();
		}
		buses.remove(hardwareId);
	}

	public void addDevice(String hardwareId, String channelAddress, String deviceAddress, String resourceName) {
		configResourceId = resourceManager.getUniqueResourceName("configModel_1");
		if (buses.get(hardwareId) != null) {
			try {
				while (configResource == null) {
					try {
						configResource = resourceManager.createResource("configResourceId",
								LemonegConfigurationModel.class);
					} catch (ResourceException e) {
						try {
							Thread.sleep(500);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					}
				}
				configResource.addOptionalElement("interfaceId");
				configResource.addOptionalElement("driverId");
				configResource.addOptionalElement("deviceAddress");
				configResource.addOptionalElement("deviceParameters");
				configResource.addOptionalElement("channelAddress");
				configResource.addOptionalElement("timeout");
				configResource.addOptionalElement("resourceName");

				configResource.interfaceId().setValue(buses.get(hardwareId).interfaceId);
				configResource.driverId().setValue(buses.get(hardwareId).driverId);
				configResource.channelAddress().setValue(channelAddress);
				configResource.deviceAddress().setValue(deviceAddress);
				configResource.deviceParameters().setValue(buses.get(hardwareId).deviceParameters);
				configResource.timeout().setValue(Integer.parseInt(buses.get(hardwareId).timeout));
				configResource.resourceName().setValue(resourceName);

				if (buses.get(hardwareId).interfaceId != null)
					configResource.activate(true);
				appManager.getHardwareManager().addListener(this);
			} catch (ResourceException e1) {
				System.out.println("catched \n");
				e1.printStackTrace();
			}
		}
	}

	public void removeDevice(String hardwareId, String resourceName) {
		buses.get(hardwareId).devices.get(resourceName).close();
		buses.get(hardwareId).devices.remove(resourceName);
	}

	public JSONArray getBusesDevicesJSONArray() {
		JSONArray BusesDevicesJSONArray = new JSONArray();
		JSONObject busJSONObject = new JSONObject();

		for (LemonegBus bus : buses.values()) {
			try {
				busJSONObject.put(bus.hardwareId, "bus");
				for (LemonegDevice device : bus.devices.values()) {
					busJSONObject.put(device.getDataResourceId(), "device");
				}

				BusesDevicesJSONArray.put(busJSONObject);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			busJSONObject = null;
		}
		return BusesDevicesJSONArray;
	}

	public LemonegDataModel getData(String hardwareId, String resourceName) {
		System.out.println("voltage:"
				+ buses.get(hardwareId).devices.get(resourceName).getDataResource().voltage().reading().getValue());
		System.out.println("current:"
				+ buses.get(hardwareId).devices.get(resourceName).getDataResource().current().reading().getValue());
		System.out.println("power:"
				+ buses.get(hardwareId).devices.get(resourceName).getDataResource().activePower().reading().getValue());
		System.out.println("frequency:"
				+ buses.get(hardwareId).devices.get(resourceName).getDataResource().phaseFrequency().getValue());
		System.out.println("time:"
				+ buses.get(hardwareId).devices.get(resourceName).getDataResource().timeStamp().getValue());

		return (buses.get(hardwareId).devices.get(resourceName).getDataResource());
	}

	public void printData(String hardwareId, String resourceName) {
		System.out.println("voltage:"
				+ buses.get(hardwareId).devices.get(resourceName).getDataResource().voltage().reading().getValue());
		System.out.println("current:"
				+ buses.get(hardwareId).devices.get(resourceName).getDataResource().current().reading().getValue());
		System.out.println("power:"
				+ buses.get(hardwareId).devices.get(resourceName).getDataResource().activePower().reading().getValue());
		System.out.println("frequency:"
				+ buses.get(hardwareId).devices.get(resourceName).getDataResource().phaseFrequency().getValue());
		System.out.println("time:"
				+ buses.get(hardwareId).devices.get(resourceName).getDataResource().timeStamp().getValue());
	}

	private void printConnectedHardware(ApplicationManager appManager) {
		HardwareManager hwMan = appManager.getHardwareManager();

		System.out.println("Connected Hardware");
		for (HardwareDescriptor desc : hwMan.getHardwareDescriptors()) {
			if (desc.getHardwareType() == HardwareDescriptor.HardwareType.USB)
				System.out.println(desc + ":" + ((UsbHardwareDescriptor) desc).getInfo().get("product"));
		}
	}

	private String findHwDescriptor(ApplicationManager appManager, String reference) {

		String result = null;

		for (HardwareDescriptor desc : appManager.getHardwareManager().getHardwareDescriptors()) {
			if (descriptorMatches(desc, reference)) {
				result = desc.getIdentifier();
				break;
			}
		}
		return result;
	}

	private boolean descriptorMatches(HardwareDescriptor desc, String reference) {
		String[] substrings = reference.split(":");
		String vid = substrings[3];
		String pid = substrings[4];
		String serial = substrings[5];

		if (desc.getHardwareType() == HardwareDescriptor.HardwareType.USB) {
			Map<String, String> info = ((UsbHardwareDescriptor) desc).getInfo();

			if (info.get("idVendor").equals(vid) && info.get("idProduct").equals(pid)
					&& info.get("serial").equals(serial) && ((UsbHardwareDescriptor) desc).getPortName() != null) {
				System.out.println("Found matching device: " + desc);
				return true;
			}

		}
		return false;
	}

	@Override
	public void hardwareAdded(HardwareDescriptor descriptor) {
		System.out.println("hardwareAdded: " + descriptor);
		// If the device matches and has a portName update the config resource
		if (descriptorMatches(descriptor, hwid)) {
			configResource.interfaceId().setValue(descriptor.getIdentifier());
			configResource.activate(true);
		}
	}

	@Override
	public void hardwareRemoved(HardwareDescriptor descriptor) {
		System.out.println("hardwareAdded: " + descriptor);
		// if someone pulled the plug, disable the resource
		if (descriptorMatches(descriptor, hwid)) {
			configResource.deactivate(true);
		}
	}

}
