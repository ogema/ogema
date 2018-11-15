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
package org.ogema.pattern.test;

import java.io.PrintStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.ogema.core.administration.AdminApplication;
import org.ogema.core.administration.RegisteredAccessModeRequest;
import org.ogema.core.administration.RegisteredStructureListener;
import org.ogema.core.application.AppID;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.pattern.PatternListener;
import org.ogema.pattern.test.pattern.RoomPattern;
import org.ogema.pattern.test.pattern.RoomPatternGreedy;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

/**
 *
 * @author Timo Fischer, Fraunhofer IWES
 */
@ExamReactorStrategy(PerClass.class)
public class AccessModeTest extends OsgiTestBase {

	CountDownLatch app2startLatch = new CountDownLatch(1);
	ApplicationManager appMan2;
	volatile int availableToApp2 = 0;
	volatile CountDownLatch app2actionLatch;

	Application app2 = new Application() {

		@Override
		public void start(ApplicationManager appManager) {
			appMan2 = appManager;
			appMan2.getResourcePatternAccess().addPatternDemand(RoomPatternGreedy.class, listener,
					AccessPriority.PRIO_HIGHEST);
			app2startLatch.countDown();
		}

		@Override
		public void stop(Application.AppStopReason reason) {

		}

		private final PatternListener<RoomPatternGreedy> listener = new PatternListener<RoomPatternGreedy>() {

			@Override
			public void patternAvailable(RoomPatternGreedy pattern) {
				availableToApp2 += 1;
				if (app2actionLatch != null) {
					app2actionLatch.countDown();
				}
			}

			@Override
			public void patternUnavailable(RoomPatternGreedy pattern) {
				availableToApp2 -= 1;
				if (app2actionLatch != null) {
					app2actionLatch.countDown();
				}
			}
		};
	};

	@Before
	public void register2ndApp() throws InterruptedException {
		ctx.registerService(Application.class, app2, null);
		assertTrue(app2startLatch.await(5, TimeUnit.SECONDS));
	}

	class RoomListener implements PatternListener<RoomPattern> {

		public final CountDownLatch foundLatch1 = new CountDownLatch(1);
		public final CountDownLatch lostLatch1 = new CountDownLatch(1);

		public RoomListener() {
		}

		@Override
		public void patternAvailable(RoomPattern pump) {
			foundLatch1.countDown();
		}

		@Override
		public void patternUnavailable(RoomPattern pump) {
			lostLatch1.countDown();
		}
	}

	void deleteAllResources() {
		for (Resource resource : appMan.getResourceAccess().getToplevelResources(Resource.class)) {
			resource.delete();
		}
	}

	void printAppInfo(PrintStream out) {
		for (AdminApplication aa : getApplicationManager().getAdministrationManager().getAllApps()) {
			printAppInfo(aa.getID(), out);
		}
	}

	void printAppInfo(AppID id, PrintStream out) {
		AdminApplication aa = getApplicationManager().getAdministrationManager().getAppById(id.getIDString());
		out.println(aa.getID());
		for (RegisteredStructureListener rsl : aa.getStructureListeners()) {
			System.out.printf("  (Structure) %s: %s%n", rsl.getResource(), rsl.getListener());
		}
		for (RegisteredAccessModeRequest ramr : aa.getAccessModeRequests()) {
			System.out.printf("  (Access)    %s: %s/%s (%b)%n", ramr.getResource().getPath(), ramr
					.getRequiredAccessMode(), ramr.getPriority(), ramr.isFulfilled());
		}
	}

	// FIXME This test is flawed... the exclusive write access on RoomPatternGreedy#name of app2 blocks 
	// access to the required field #name of RoomPattern
	@Test
	@Ignore
	public void incompleteGreedyPatternDoNotBlockComletion() throws InterruptedException {

		deleteAllResources();
		final RoomListener listener = new RoomListener();
		//advAcc.addPatternDemand(RoomPattern.class, listener, AccessPriority.PRIO_LOWEST);
		final RoomPattern roomPattern = advAcc.createResource("MY_ROOM_IS_MY_CASTLE", RoomPattern.class);
		// model does not match the greedy 2nd app's demand, so it should not block.
		roomPattern.model.activate(true);
		assertTrue(roomPattern.model.name().exists());
		assertTrue(roomPattern.model.name().isActive());

		advAcc.addPatternDemand(RoomPattern.class, listener, AccessPriority.PRIO_LOWEST);

		AdminApplication aapp = getApplicationManager().getAdministrationManager().getAppById(
				getApplicationManager().getAppID().getIDString());
		assertNotNull(aapp);

		if (!listener.foundLatch1.await(5, TimeUnit.SECONDS)) {
			printAppInfo(System.out);
			fail("App1 did not get the Pattern");
		}

		printAppInfo(System.out);
		//assertTrue("App1 gets the Pattern", listener.foundLatch1.await(5, TimeUnit.SECONDS));
		//assertEquals(0, listener.foundLatch1.getCount());
		assertEquals(0, availableToApp2);

		//??? shouldn't this work too?
		//assertFalse("App1 patternUnavailable called at wrong time", listener.lostLatch1.await(3, TimeUnit.SECONDS));
		// create extra field so that greedy 2nd app's demand is matches. Must lose pattern
		app2actionLatch = new CountDownLatch(1);
		roomPattern.model.temperatureSensor().create().activate(false);

		assertTrue("App1 loses Pattern", listener.lostLatch1.await(5, TimeUnit.SECONDS));
		assertEquals(0, listener.lostLatch1.getCount());

		assertTrue(roomPattern.model.name().isActive());
		assertTrue("App2 gets the Pattern", app2actionLatch.await(5, TimeUnit.SECONDS));
		//app2actionLatch.await();

		assertEquals("App2 gets the Pattern", 1, availableToApp2);
	}

}
