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
package org.ogema.tools.resource.util.test;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.exam.OsgiAppTestBase;
import org.ogema.tools.resource.util.LoggingUtils;

/**
 * Tests for resource utils tool.
 * 
 */
public class LoggingTest extends OsgiAppTestBase {

	private ResourceManagement rm;
//	private ResourceAccess ra;

	public LoggingTest() {
		super(true);
	}

	@Before
	public void setup() {
		rm = getApplicationManager().getResourceManagement();
//		ra = getApplicationManager().getResourceAccess();
	}

	@Test
	public void testLoggingUtils() throws InterruptedException {
		FloatResource res = rm.createResource("floatRes", FloatResource.class);
		res.activate(true);
		LoggingUtils.activateLogging(res, 10);
		assertTrue("Logging unexpectedly not enabled", LoggingUtils.isLoggingEnabled(res));
		Thread.sleep(500);
		res.setValue(324.34F);
		Thread.sleep(500);
		List<SampledValue> list = LoggingUtils.getHistoricalData(res).getValues(0);
		// FIXME
		System.out.println("   List size: " + list.size());
		//		assert(list.size() > 1) : "Log data missing";  // FIXME not working
		LoggingUtils.deactivateLogging(res);
		assert (LoggingUtils.getHistoricalData(res).getConfiguration() == null) : "Expected deactivated logging, but found it active";
		res.delete();
	}

}
