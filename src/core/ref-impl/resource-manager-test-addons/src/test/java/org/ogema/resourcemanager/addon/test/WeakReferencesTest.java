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
package org.ogema.resourcemanager.addon.test;

import java.util.Random;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.ogema.core.model.Resource;
import org.ogema.core.model.schedule.AbsoluteSchedule;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.exam.OsgiAppTestBase;
import org.ogema.model.locations.Room;
import org.ogema.model.sensors.TemperatureSensor;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

@ExamReactorStrategy(PerClass.class)
public class WeakReferencesTest extends OsgiAppTestBase {
	
	private final static boolean ignore = "true".equalsIgnoreCase(System.getenv("NO_LONG_TESTS")) || Boolean.getBoolean("NO_LONG_TESTS");

	// long running test, ~ 1 minute; verifies that weak referencing of virtual resources does not cause a problem
	// (the goal is sonewhat implementation specific, but the test does not rely on any implementation details) 
	// several related checks are performed in the same test; splitting it would overly increase the total test runtime
	@Test
	public void virtualSubresourcesAreNotReturnedAsNull() throws InterruptedException {
		Assume.assumeFalse(ignore);
		final int innerLoopLimit = 2000;
		final int outerLoopLimit = 1000;
		// run test for at most 1min; this is required, because otherwise the execution time of the test would
		// be strongly dependent on the host machine, and it might run very long 
		long timeout = 60000;
		final Runtime r = Runtime.getRuntime();
		System.out.println("Running virtual subresources test on " + r.availableProcessors() + " cpus. Total memory: " + 
				getMB(r.totalMemory()) + "MB, max memory: " + getMB(r.maxMemory()) + "MB");
		long start = System.currentTimeMillis();
		for (int i=0;i<outerLoopLimit;i++) {
			Resource resource = getApplicationManager().getResourceManagement().createResource(newResourceName(), TemperatureSensor.class);
			resource.getSubResource("existingDecorator", Room.class).create();
			Class<? extends Resource> backup = null;
			for (int j=0;j<innerLoopLimit;j++) {
				Class<? extends Resource> type = getRandomType();
				Resource subresource;
				if (Schedule.class.isAssignableFrom(type)) {
					FloatResource f = resource.getSubResource("sub" + j, FloatResource.class);
					Assert.assertNotNull(f);
					subresource = f.getSubResource("sub" + j, type);
				}
				else
					subresource = resource.getSubResource("sub" + j, type);
				Assert.assertNotNull("Virtual subresource found null",subresource);
				// now check again for a virtual subresource that did exist previously
				if (j > 0 && !Schedule.class.isAssignableFrom(backup)) {
					Resource res =  resource.getSubResource("sub" + (j-1), backup);
					Assert.assertNotNull("Virtual subresource found null",res);
					res = null;
					res = resource.getSubResource("sub" + (j-1)); // now it may be null, actually; just to check that no exception occurs
					if (res == null)
						System.out.println("Virtual resource has been removed");
				}
				backup = type;
				// check that existing resource still exists
				Assert.assertNotNull("Existing resource found null",resource.getSubResource("existingDecorator"));
			}
			// now check virtual optional element
			TemperatureResource reading = resource.getSubResource("reading"); // optional element in TemperatureSensor
			Assert.assertNotNull("Optional element found null",reading);
			if (i % 10 == 0)
				System.out.printf("%3d:  used memory: %5d MB,  total memory: %5d MB,  max memory: %5d MB.\n", 
						i,getMB(r.totalMemory()-r.freeMemory()), getMB(r.totalMemory()),getMB(r.maxMemory()));
			if (System.currentTimeMillis() - start > timeout)
				break;
		}
	}
	
	private static final long getMB(long bytes) {
		return bytes / 1024 / 1024;
	}
	
	private static final Random r = new Random();
	private static final Class<? extends Resource> getRandomType() {
		switch (Math.abs(r.nextInt()) % 4) {
		case 0:
			return FloatResource.class;
		case 1: 
			return AbsoluteSchedule.class;
		case 2:
			return TemperatureSensor.class;
		case 3:
			return TemperatureResource.class;
		default:
			throw new RuntimeException("??");
		}
	}
	
}
