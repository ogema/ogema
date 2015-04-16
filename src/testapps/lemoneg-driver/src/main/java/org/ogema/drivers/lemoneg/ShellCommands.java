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
package org.ogema.drivers.lemoneg;

import java.io.PrintStream;
import java.util.Hashtable;

import org.json.JSONArray;
import org.osgi.framework.BundleContext;

public class ShellCommands {
	private BundleContext context = null;
	private LemonegDriver lemoneg_driver;

	public ShellCommands(BundleContext context, LemonegDriver lemoneg_driver) {
		this.lemoneg_driver = lemoneg_driver;
		Hashtable<String, Object> props = new Hashtable<String, Object>();
		props.put("osgi.command.scope", "lemoneg");
		props.put("osgi.command.function", new String[] { "addBus", "removeBus", "addDevice", "removeDevice",
				"getBusesDevices", "getData", "printData" });
		this.context = context;
		context.registerService(this.getClass().getName(), this, props);
	}

	public void addBus(String hardwareId, String driverId, String deviceParameters, String timeout) {
		lemoneg_driver.addBus(hardwareId, driverId, deviceParameters, timeout);
	}

	public void removeBus(String hardwareId) {
		lemoneg_driver.removeBus(hardwareId);
	}

	public void addDevice(String hardwareId, String channelAddress, String deviceAddress, String resourceName) {
		lemoneg_driver.addDevice(hardwareId, channelAddress, deviceAddress, resourceName);
	}

	public void removeDevice(String hardwareId, String resourceName) {
		lemoneg_driver.removeDevice(hardwareId, resourceName);
	}

	public JSONArray getBusesDevices() {
		return lemoneg_driver.getBusesDevicesJSONArray();
	}

	public LemonegDataModel getData(String hardwareId, String resourceName) {
		return lemoneg_driver.getData(hardwareId, resourceName);
	}

	public void printData(String hardwareId, String resourceName) {
		lemoneg_driver.printData(hardwareId, resourceName);
	}

	public String getName() {
		return "listPolicies";
	}

	public String getUsage() {
		return "listPolicies";
	}

	public String help() {
		return "List all policies in system.";
	}

	public void execute(PrintStream out, PrintStream err) {

	}
}
