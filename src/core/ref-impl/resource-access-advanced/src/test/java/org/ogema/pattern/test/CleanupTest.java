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
package org.ogema.pattern.test;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.ogema.core.administration.AdminApplication;
import org.ogema.core.administration.RegisteredPatternListener;
import org.ogema.core.administration.RegisteredResourceDemand;
import org.ogema.core.administration.RegisteredStructureListener;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.pattern.ResourcePatternAccess;
import org.ogema.exam.OsgiAppTestBase;
import org.ogema.exam.PatternTestListener;
import org.ogema.model.devices.generators.HeatPump;
import org.ogema.model.devices.storage.ElectricityStorage;
import org.ogema.pattern.test.pattern.HeatPumpPattern2;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

import junit.framework.Assert;

@ExamReactorStrategy(PerClass.class)
public class CleanupTest extends OsgiAppTestBase {

	ResourcePatternAccess rpa;
	AdminApplication adminApp;
	
	@Before
	public void setAdmin() {
		rpa = getApplicationManager().getResourcePatternAccess();
		adminApp = getApplicationManager().getAdministrationManager().getAppById(getApplicationManager().getAppID().getIDString());
		Assert.assertNotNull("Admin app not found", adminApp);
	}

	// verifies that all listeners used internally by the pattern access are removed when the demanded model is deleted. 
	// This is an implementation-specific test, may need adaptation if the pattern access implementation changes
	@Test
	public void listenersAreUnregisteredForDeletedResource() throws InterruptedException {
		PatternTestListener<HeatPumpPattern2> listener = new PatternTestListener<>();
		rpa.addPatternDemand(HeatPumpPattern2.class, listener, AccessPriority.PRIO_LOWEST);
		HeatPumpPattern2 pattern = rpa.createResource(newResourceName(), HeatPumpPattern2.class);
		rpa.activatePattern(pattern);
		
		Assert.assertTrue("Pattern found event pending",listener.awaitFoundEvent(5, TimeUnit.SECONDS));
		List<RegisteredPatternListener> listeners = adminApp.getPatternListeners();
		Assert.assertEquals("Unexpected number of pattern listeners. There should be one, got: " + listeners.size(), 1, listeners.size());
		List<RegisteredResourceDemand> resourceDemands = adminApp.getResourceDemands();
		Assert.assertEquals("Unexpected number of resource demands. There should be one, got: " + resourceDemands.size(), 1, resourceDemands.size());
		List<RegisteredStructureListener> structureListeners = adminApp.getStructureListeners();
		Assert.assertTrue("Pattern access did not register any structure listeners", structureListeners.size() > 0);
//		List<RegisteredValueListener> valueListeners = adminApp.getValueListeners();
//		Assert.assertTrue("Pattern access did not register any value listeners", valueListeners.size() > 0); // nonsense, not required
		pattern.model.delete();
		Assert.assertTrue("Pattern lost event missing",listener.awaitLostEvent(5, TimeUnit.SECONDS));
		int zombies = waitForListenersRemoval();
		Assert.assertEquals(zombies + " pattern-internal structure and/or value listeners survived demanded model deletion",0,zombies);
		int zombieAMs = waitForAccessModeRequestsRemoval();
		Assert.assertEquals(zombieAMs + " access mode requests survived demanded model deletion",0,zombies);
		rpa.removePatternDemand(HeatPumpPattern2.class, listener);
		listeners = adminApp.getPatternListeners();
		Assert.assertEquals("Unexpected pattern listeners found: " + listeners.size(), 0, listeners.size());
		resourceDemands = adminApp.getResourceDemands();
		Assert.assertEquals("Unexpected demand listeners found: " + resourceDemands.size(), 0, resourceDemands.size());
	}
	
	// same as above, but with multiple pattern instances, including incomplete ones
	@Test
	public void listenersAreUnregisteredForDeletedResource2() throws InterruptedException {
		HeatPumpPattern2 pattern = rpa.createResource(newResourceName(), HeatPumpPattern2.class);
		rpa.activatePattern(pattern);
		PatternTestListener<HeatPumpPattern2> listener = new PatternTestListener<>();
		listener.reset(2);
		rpa.addPatternDemand(HeatPumpPattern2.class, listener, AccessPriority.PRIO_LOWEST);
		HeatPumpPattern2 pattern2 = rpa.createResource(newResourceName(), HeatPumpPattern2.class);
		rpa.activatePattern(pattern2);
		HeatPump hp = getApplicationManager().getResourceManagement().createResource(newResourceName(), HeatPump.class);
		HeatPump hp2 = getApplicationManager().getResourceManagement().createResource(newResourceName(), HeatPump.class);
		hp2.getSubResource("electricityStorage", ElectricityStorage.class).create(); // one of the pattern fields
		hp.activate(true);
		hp2.activate(true);
		
		Assert.assertTrue("Pattern found event pending",listener.awaitFoundEvent(5, TimeUnit.SECONDS));
		List<RegisteredPatternListener> listeners = adminApp.getPatternListeners();
		Assert.assertEquals("Unexpected number of pattern listeners. There should be one, got: " + listeners.size(), 1, listeners.size());
		List<RegisteredResourceDemand> resourceDemands = adminApp.getResourceDemands();
		Assert.assertEquals("Unexpected number of resource demands. There should be one, got: " + resourceDemands.size(), 1, resourceDemands.size());
		List<RegisteredStructureListener> structureListeners = adminApp.getStructureListeners();
		Assert.assertTrue("Pattern access did not register any structure listeners", structureListeners.size() > 0);

		pattern.model.delete();
		pattern2.model.delete();
		hp.delete();
		hp2.delete();
		
		Assert.assertTrue("Pattern lost event missing",listener.awaitLostEvent(5, TimeUnit.SECONDS));
		int zombies = waitForListenersRemoval();
		Assert.assertEquals(zombies + " pattern-internal structure and/or value listeners survived demanded model deletion",0,zombies);
		int zombieAMs = waitForAccessModeRequestsRemoval();
		Assert.assertEquals(zombieAMs + " access mode requests survived demanded model deletion",0,zombies);
		rpa.removePatternDemand(HeatPumpPattern2.class, listener);
		listeners = adminApp.getPatternListeners();
		Assert.assertEquals("Unexpected pattern listeners found: " + listeners.size(), 0, listeners.size());
		resourceDemands = adminApp.getResourceDemands();
		Assert.assertEquals("Unexpected demand listeners found: " + resourceDemands.size(), 0, resourceDemands.size());
	}

	// wait for at most 5s until all listeners have been removed
	int waitForListenersRemoval() throws InterruptedException {
		for (int i=0;i<50;i++) {
			if (adminApp.getStructureListeners().isEmpty() && adminApp.getValueListeners().isEmpty()) 
				return 0;
			Thread.sleep(100);
		}
		return adminApp.getStructureListeners().size() + adminApp.getValueListeners().size();
	}
	
	// wait for at most 5s until all access mode requests have been removed
	int waitForAccessModeRequestsRemoval() throws InterruptedException {
		for (int i=0;i<50;i++) {
			if (adminApp.getAccessModeRequests().isEmpty()) 
				return 0;
			Thread.sleep(100);
		}
		return adminApp.getAccessModeRequests().size();
	}
	
}
