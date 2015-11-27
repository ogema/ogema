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
package org.ogema.experimental;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.Test;
import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.pattern.PatternListener;
import org.ogema.model.devices.buildingtechnology.Thermostat;
import org.ogema.model.locations.Room;
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

		public String lastAvailable = null;
		public String lastUnavailable = null;

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

	@Ignore
	@Test
	public void deleteSubresourceAndSetAsReference() throws InterruptedException {
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
		assertEquals(subRoom, listener.lastAvailable);
		listener.resetLatches();
		room.activate(true);
		listener.foundLatch.await(5, TimeUnit.SECONDS);
		assertEquals(topRoom, listener.lastAvailable);
		listener.resetLatches();
		thermo.location().room().setAsReference(room);
		listener.lostLatch.await(5, TimeUnit.SECONDS);
		assertEquals(subRoom, listener.lastUnavailable); // make sure the new reference is not set before the callback has been executed
	}

}
