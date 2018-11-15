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
package org.ogema.drivers.hmhl.test;

import org.junit.Before;
import org.junit.Test;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.driver.hmhl.HM_hlConfig;
import org.ogema.driver.hmhl.HM_hlDriver;
import org.ogema.exam.OsgiAppTestBase;
import org.ogema.model.devices.buildingtechnology.Thermostat;
import org.ops4j.pax.exam.Option;


public class ResourceCreationTest extends OsgiAppTestBase {

	private ResourceManagement rm;
	private ResourceAccess ra;

	public ResourceCreationTest() {
		super(true);
	}

	@Before
	public void setup() {
		rm = getApplicationManager().getResourceManagement();
		ra = getApplicationManager().getResourceAccess();
	}
	
	/*
	 * Start driver dependencies
	 */
	@Override
	public Option[] frameworkBundles() {
		Option[] opt = super.frameworkBundles();
		Option[] options = new Option[opt.length + 2];
		for (int i =0;i<opt.length;i++) {
			options[i] = opt[i];
		}
		options[opt.length] = felixGogoShellOption();
		options[opt.length+1] = webConsoleOption();
		return options;
	}
	
	@Test
	public void createAndActivateThermostat() {
		String resourceName = "testThermo";
		HM_hlConfig cfg = new HM_hlConfig();
		cfg.deviceId=resourceName;cfg.resourceName=resourceName;cfg.channelAddress="abc:def";
		cfg.driverId="homematic";cfg.interfaceId="homematicusb";cfg.deviceParameters="moin";
		cfg.deviceAddress="HM:1000";
		new org.ogema.driver.hmhl.devices.Thermostat(new HM_hlDriver(), getApplicationManager(), cfg);
		Thermostat thermostat = ra.getResource(resourceName);
		String msg = "Resource unexpectedly found inactive: ";
		assert(thermostat.isActive()) : msg + thermostat.getLocation();
		assert(thermostat.temperatureSensor().isActive())  : msg + thermostat.temperatureSensor().getLocation();
		assert(thermostat.battery().isActive())  : msg + thermostat.battery().getLocation();
		assert(thermostat.valve().setting().isActive())  : msg + thermostat.valve().setting().getLocation();		
		thermostat.delete();
		
	}

}