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
