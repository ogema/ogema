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
package org.ogema.tools.rource.util.test;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.exam.OsgiAppTestBase;
import org.ogema.tools.resource.util.LoggingUtils;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;

/**
 * Tests for resource utils tool.
 * 
 */
public class LoggingTest extends OsgiAppTestBase {

	private ResourceManagement rm;
	private ResourceAccess ra;

	public LoggingTest() {
		super(true);
	}

	@Before
	public void setup() {
		rm = getApplicationManager().getResourceManagement();
		ra = getApplicationManager().getResourceAccess();
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
