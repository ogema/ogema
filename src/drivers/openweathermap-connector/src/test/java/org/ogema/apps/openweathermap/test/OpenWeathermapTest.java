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
package org.ogema.apps.openweathermap.test;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.ogema.apps.openweathermap.OpenWeatherMapApplicationI;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.exam.OsgiAppTestBase;
import org.ogema.model.locations.Room;
import org.ogema.model.sensors.SolarIrradiationSensor;

import static org.junit.Assert.assertTrue;

@Ignore("API key required for tests; see method #beforeClass() below")
public class OpenWeathermapTest extends OsgiAppTestBase {

	public OpenWeathermapTest() {
		super(true);
	}

	@Inject
	OpenWeatherMapApplicationI openWeatherMapApp;

	@BeforeClass
	public static void beforeClass() {

		// To run the test, uncomment this line and enter your key as value; comment out the line after, that throws the
		// exception
		// System.getProperties().setProperty("org.ogema.drivers.openweathermap.key", "");
		throw new IllegalStateException("OpenWeatherMap key not set, cannot run test");

	}

	@AfterClass
	@BeforeClass
	public static void delete() {
		// deleteData();
		try {
			FileUtils.deleteDirectory(new File("data"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
		}
	}

	private ResourceManagement resourceManagement;
	private ResourceAccess resourceAccess;

	@Before
	public void init() {

		resourceManagement = getApplicationManager().getResourceManagement();
		resourceAccess = getApplicationManager().getResourceAccess();
	}

	@Test(timeout = 60000)
	public void test() {

		Room room = (Room) openWeatherMapApp.createEnvironment("test", "Kassel", "de");

		sleep();
		assertTrue(room.temperatureSensor().reading().forecast().getValues(0).size() >= 1440);
		assertTrue(room.getSubResource("solarIrradiationSensor", SolarIrradiationSensor.class).reading().forecast()
				.getValues(0).size() >= 1440);
	}

	private void sleep() {
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
