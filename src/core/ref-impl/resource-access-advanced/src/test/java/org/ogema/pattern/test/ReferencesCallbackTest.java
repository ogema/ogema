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

import org.ogema.pattern.test.pattern.HeaterPattern;
import org.ogema.pattern.test.pattern.RoomPattern;
import org.ogema.pattern.test.pattern.RoomPatternGreedy;

import static org.junit.Assert.assertEquals;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.ogema.core.administration.AdminApplication;
import org.ogema.core.administration.PatternCondition;
import org.ogema.core.administration.RegisteredPatternListener;
import org.ogema.core.logging.LogLevel;
import org.ogema.core.logging.LogOutput;
import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.ResourceDemandListener;
import org.ogema.core.resourcemanager.pattern.PatternListener;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.exam.PatternTestListener;
import org.ogema.exam.ResourceAssertions;

import static org.ogema.exam.ResourceAssertions.assertActive;
import static org.ogema.exam.ResourceAssertions.assertExists;
import org.ogema.exam.StructureTestListener;
import org.ogema.model.devices.buildingtechnology.Thermostat;
import org.ogema.model.devices.generators.ElectricHeater;
import org.ogema.model.locations.Room;
import org.ogema.model.sensors.TemperatureSensor;
import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

@ExamReactorStrategy(PerClass.class)
public class ReferencesCallbackTest extends OsgiTestBase {

	@ProbeBuilder
	public TestProbeBuilder build(TestProbeBuilder builder) {
		builder.setHeader("Bundle-SymbolicName", "test-probe");
		return builder;
	}

	class RoomListener implements PatternListener<RoomPattern> {

		public List<RoomPattern> availablePatterns;

		public CountDownLatch foundLatch;
		public CountDownLatch lostLatch;

		public volatile String lastAvailable = null;
		public volatile String lastUnavailable = null;

		public RoomListener() {
            availablePatterns = new LinkedList<>();
            resetLatches();
        }

		public final void resetLatches() {
			foundLatch = new CountDownLatch(1);
			lostLatch = new CountDownLatch(1);
		}

		@Override
		public void patternAvailable(RoomPattern rm) {
			// FIXME
			System.out.println("    ReferenceCallbackTest: patternAvailable " + rm.model.getPath() + ", location "
					+ rm.model.getLocation());
			lastAvailable = rm.name.getValue();
			if (availablePatterns.contains(rm)) {
				throw new RuntimeException("Available callback for the same pattern received twice");
			}
			else {
				availablePatterns.add(rm);
			}
			foundLatch.countDown(); // must come last for timing purposes
		}

		@Override
		public void patternUnavailable(RoomPattern rm) {
			// FIXME
			System.out.println("    ReferenceCallbackTest: patternUnavailable " + rm.model.getPath() + ", location "
					+ rm.model.getLocation());
			lastUnavailable = rm.name.getValue();
			if (!availablePatterns.contains(rm)) {
				throw new RuntimeException("Unavailable callback received without corresponding available callback");
			}
			availablePatterns.remove(rm);
			lostLatch.countDown(); // must come last for timing purposes           
		}
	}

	private void deleteAllResources() {
		final List<Resource> resources = resAcc.getToplevelResources(Resource.class);
		for (Resource resource : resources)
			resource.delete();
	}

	@Test
	public void addingReferenceCausesExistCallback() throws InterruptedException {
		deleteAllResources();
		Room room = resMan.createResource("myReferencingRoom", Room.class);
		room.activate(false);
		StringResource name = resMan.createResource("myReferencedRoomName", StringResource.class);
		name.activate(false);

		RoomListener listener = new RoomListener();
		advAcc.addPatternDemand(RoomPattern.class, listener, AccessPriority.PRIO_LOWEST);
		listener.foundLatch.await(2, TimeUnit.SECONDS);
		assertEquals(0, listener.availablePatterns.size());

		listener.resetLatches();
		room.name().setAsReference(name);
		listener.foundLatch.await(5, TimeUnit.SECONDS);
		assertEquals(1, listener.availablePatterns.size());

		listener.resetLatches();
		room.name().delete();
		listener.lostLatch.await(5, TimeUnit.SECONDS);
		assertEquals(0, listener.availablePatterns.size());
	}

	@Test
	public void setExistingResourceToReference() throws InterruptedException {
		deleteAllResources();
		String subRoom = "subRoom";
		String topRoom = "topRoom";
		Thermostat thermo = resMan.createResource("randomThermostat", Thermostat.class);
		thermo.location().room().name().create();
		thermo.location().room().name().setValue(subRoom);
		Room room = resMan.createResource(topRoom, Room.class);
		room.name().create();
		room.name().setValue(topRoom);
		RoomListener listener = new RoomListener();
		advAcc.addPatternDemand(RoomPattern.class, listener, AccessPriority.PRIO_LOWEST);
		thermo.activate(true);
		listener.foundLatch.await(5, TimeUnit.SECONDS);
		listener.resetLatches();
		room.activate(true);
		listener.foundLatch.await(5, TimeUnit.SECONDS);
		listener.resetLatches();
		assertEquals(2, listener.availablePatterns.size());
		thermo.location().room().setAsReference(room);
		listener.lostLatch.await(5, TimeUnit.SECONDS);
		Thread.sleep(500);
		assertEquals(0, listener.lostLatch.getCount());
		assertEquals(1, listener.availablePatterns.size());
		RoomPattern pt = listener.availablePatterns.get(0);
		// FIXME
		System.out
				.println("   Test: last pattern path: " + pt.model.getPath() + ", location " + pt.model.getLocation());
		assertEquals(room.getPath(), pt.model.getPath());
		advAcc.removePatternDemand(RoomPattern.class, listener);
		thermo.delete();
		room.delete();
	}

	class HeaterListener implements PatternListener<HeaterPattern> {

		public volatile CountDownLatch foundLatch;
		public volatile CountDownLatch lostLatch;

		public HeaterListener() {
			resetLatches();
		}

		public final void resetLatches() {
			foundLatch = new CountDownLatch(1);
			lostLatch = new CountDownLatch(1);
		}

		@Override
		public void patternAvailable(HeaterPattern rm) {
			// FIXME
			//System.out.println("  Double reference pattern available");
            getApplicationManager().getLogger().info("double reference pattern available");
			foundLatch.countDown();
		}

		@Override
		public void patternUnavailable(HeaterPattern rm) {
			// FIXME
			//System.out.println("  Double reference pattern unavailable");
            getApplicationManager().getLogger().info("double reference pattern unavailable");
			lostLatch.countDown();
		}
	}
    
    @Test
	public void doubleReferences_Loop() throws InterruptedException {
        for (int i = 0; i < 5000; i++) {
            doubleReferences();
            System.out.printf("-------------- %d -----------------%n", i++);
        }
    }
    
    //@Ignore("callback missing")
	@Test
	public void doubleReferences() throws InterruptedException {
        getApplicationManager().getLogger().setMaximumLogLevel(LogOutput.CONSOLE, LogLevel.TRACE);
        HeaterListener listener = new HeaterListener();
        advAcc.addPatternDemand(HeaterPattern.class, listener, AccessPriority.PRIO_LOWEST);
        ElectricHeater a = resMan.createResource(newResourceName(), ElectricHeater.class);
        Room b = resMan.createResource(newResourceName(), Room.class);
        TemperatureSensor c = resMan.createResource(newResourceName(), TemperatureSensor.class);
        a.activate(true);
        b.activate(true);
        c.activate(true);
        a.location().room().setAsReference(b);
        b.temperatureSensor().setAsReference(c);
        c.reading().create();
        c.reading().activate(false);
        assertActive(c.reading());
        AdminApplication aa = getApplicationManager().getAdministrationManager().getAppById(getApplicationManager().getAppID().toString());
        assertTrue("pattern listener registered", !aa.getPatternListeners().isEmpty());
        for (RegisteredPatternListener l : aa.getPatternListeners()) {
            for (ResourcePattern<?> p : l.getIncompletePatterns()) {
                for (PatternCondition pc : l.getConditions(p)) {
                    System.out.printf("unmet condition: %s (%s)%n", pc.getFieldName(), pc.getPath());
                }
            }
            System.out.println("completed patterns: " + l.getCompletedPatterns());
        }
        assertExists(a.location().room().temperatureSensor().reading());
        assertActive(a.location().room().temperatureSensor().reading());
        assertTrue("receive patternAvailable callback", listener.foundLatch.await(5, TimeUnit.SECONDS));
        //assertEquals("Missing patternAvailable callback;", 0, listener.foundLatch.getCount());
        c.delete();
        assertTrue("receive patternUnavailable callback", listener.lostLatch.await(5, TimeUnit.SECONDS));
        //assertEquals("Missing patternUnvailable callback;", 0, listener.lostLatch.getCount());
        b.delete();
        a.delete();
        advAcc.removePatternDemand(HeaterPattern.class, listener);
    }

//	@Ignore("'failure' is actually consistent with defined behaviour: callbacks only run after changes are complete")
//    //see AssemblerBase#patternUnavailable
//	@Test
//	public void deleteSubresourceAndSetAsReference() throws InterruptedException {
//		String subRoom = "subRoom";
//		String topRoom = "topRoom";
//		Thermostat thermo = resMan.createResource("randomThermostat", Thermostat.class);
//		thermo.location().room().name().create();
//		thermo.location().room().name().setValue(subRoom);
//		Room room = resMan.createResource(topRoom, Room.class);
//		room.name().create();
//		room.name().setValue(topRoom);
//		RoomListener listener = new RoomListener();
//		advAcc.addPatternDemand(RoomPattern.class, listener, AccessPriority.PRIO_LOWEST);
//		thermo.activate(true);
//		listener.foundLatch.await(5, TimeUnit.SECONDS);
//		assertEquals(subRoom, listener.lastAvailable);
//		listener.resetLatches();
//		room.activate(true);
//		listener.foundLatch.await(5, TimeUnit.SECONDS);
//		assertEquals(topRoom, listener.lastAvailable);
//		listener.resetLatches();
//        listener.lastUnavailable = null;
//		thermo.location().room().setAsReference(room);
//		assertTrue("expecting patternUnavailable callback", listener.lostLatch.await(5, TimeUnit.SECONDS));
//		assertEquals(subRoom, listener.lastUnavailable); // make sure the new reference is not set before the callback has been executed
//	}
	
	@Test
	public void deleteSubresourceAndSetAsReference() throws InterruptedException {
		
		PatternTestListener<RoomPatternGreedy> listener = new PatternTestListener<>();
		advAcc.addPatternDemand(RoomPatternGreedy.class, listener, AccessPriority.PRIO_LOWEST);
		Room room = resMan.createResource(newResourceName(), Room.class);
		TemperatureSensor ts = resMan.createResource(newResourceName(), TemperatureSensor.class);
		room.name().create();
		room.temperatureSensor().create();
		room.activate(true);
		ts.activate(true);
		assertTrue(listener.awaitFoundEvent(5, TimeUnit.SECONDS));
		final RoomPatternGreedy pattern = listener.lastAvailable;
		listener.reset();
		pattern.temperatureSensor.setAsReference(ts);
		assertTrue(listener.awaitLostEvent(5, TimeUnit.SECONDS));
		assertTrue(listener.awaitFoundEvent(5, TimeUnit.SECONDS));
		TemperatureResource tr = AccessController.doPrivileged(new PrivilegedAction<TemperatureResource>() {

			@Override
			public TemperatureResource run() {
				return pattern.temperatureSensor.reading();
			}
		});
		tr.create();
		ResourceAssertions.assertExists(tr);
		ResourceAssertions.assertLocationsEqual(tr, ts.reading());
		
		room.delete();
		ts.delete();
	}
	

}
