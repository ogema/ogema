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