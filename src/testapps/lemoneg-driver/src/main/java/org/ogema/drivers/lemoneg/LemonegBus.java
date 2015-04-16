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

import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceDemandListener;
import org.ogema.core.resourcemanager.ResourceManagement;

/**
 * 
 */
public class LemonegBus {

	// public Map<LemonegConfigurationModel, LemonegDevice> devices;
	public Map<String, LemonegDevice> devices; // identify LemonegDevice by resourceName
	public String hardwareId;
	public String interfaceId;
	public String driverId;
	public String deviceParameters;
	public String timeout;

	public LemonegBus(String hardwareId, String driverId, String deviceParameters, String timeout) {
		devices = new HashMap<String, LemonegDevice>();
		this.hardwareId = hardwareId;
		this.driverId = driverId;
		this.deviceParameters = deviceParameters;
		this.timeout = timeout;
	}

}
