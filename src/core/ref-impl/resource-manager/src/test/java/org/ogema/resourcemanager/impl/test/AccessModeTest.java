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
package org.ogema.resourcemanager.impl.test;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.model.Resource;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.resourcemanager.AccessMode;

import static org.ogema.core.resourcemanager.AccessMode.EXCLUSIVE;
import static org.ogema.core.resourcemanager.AccessMode.READ_ONLY;
import static org.ogema.core.resourcemanager.AccessMode.SHARED;
import org.ogema.core.resourcemanager.AccessModeListener;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.model.actors.OnOffSwitch;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

/**
 *
 * @author jlapp
 */
@ExamReactorStrategy(PerClass.class)
public class AccessModeTest extends OsgiTestBase {

	CountDownLatch app2startLatch = new CountDownLatch(1);
	Application app2 = new Application() {

		@Override
		public void start(ApplicationManager appManager) {
			appMan2 = appManager;
			app2startLatch.countDown();
		}

		@Override
		public void stop(Application.AppStopReason reason) {
			appMan2 = null;
		}

	};
	ApplicationManager appMan2;

	@Before
	public void register2ndApp() throws InterruptedException {
		ctx.registerService(Application.class, app2, null);
		assertTrue(app2startLatch.await(5, TimeUnit.SECONDS));
	}

	@Test
    public void resourceAvailableCallbackWorks() throws InterruptedException, BrokenBarrierException, TimeoutException {
            OnOffSwitch sw = getApplicationManager().getResourceManagement().createResource(newResourceName(), OnOffSwitch.class);

        final CyclicBarrier accessModeAvailableBarrier = new CyclicBarrier(2);
        final CountDownLatch accessModeUnavailableLatch = new CountDownLatch(1);
        AccessModeListener rdl = new AccessModeListener() {

            @Override
            public void accessModeChanged(Resource resource) {
                System.out.printf("resource access mode for %s = %s%n", resource, resource.getAccessMode());
                try {
                    if (resource.getAccessMode() != READ_ONLY) {
                        accessModeAvailableBarrier.await();
                    } else {
                        accessModeUnavailableLatch.countDown();
                    }
                } catch (InterruptedException | BrokenBarrierException ex) {
                    fail(ex.toString());
                }
            }

        };

        sw.stateControl().create();
        sw.activate(true);
        sw.stateControl().setValue(true);
        sw.stateControl().addAccessModeListener(rdl);
        sw.stateControl().requestAccessMode(AccessMode.SHARED, AccessPriority.PRIO_DEVICESPECIFIC);

        accessModeAvailableBarrier.await(5, TimeUnit.SECONDS);
        accessModeAvailableBarrier.reset();
        assertEquals(AccessPriority.PRIO_DEVICESPECIFIC, sw.stateControl().getAccessPriority());
        assertEquals(SHARED, sw.stateControl().getAccessMode());
        assertNotNull(appMan2);
        sw.stateControl().setValue(false);

        assertEquals(1, accessModeUnavailableLatch.getCount());
        OnOffSwitch swApp2 = (OnOffSwitch) appMan2.getResourceAccess().getResource(sw.getPath());
        swApp2.stateControl().requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);
        assertTrue(accessModeUnavailableLatch.await(5, TimeUnit.SECONDS));
        assertEquals(AccessPriority.PRIO_HIGHEST, swApp2.stateControl().getAccessPriority());
        assertEquals(EXCLUSIVE, swApp2.stateControl().getAccessMode());
        assertEquals(READ_ONLY, sw.stateControl().getAccessMode());

        assertTrue(swApp2.stateControl().setValue(true));
        assertFalse(sw.stateControl().setValue(false));
        assertTrue(swApp2.stateControl().getValue());
        assertTrue(sw.stateControl().getValue());

        //end exclusive access
        swApp2.stateControl().requestAccessMode(READ_ONLY, AccessPriority.PRIO_LOWEST);
        accessModeAvailableBarrier.await(5, TimeUnit.SECONDS);
        assertEquals(SHARED, swApp2.stateControl().getAccessMode());
        assertEquals(SHARED, sw.stateControl().getAccessMode());
        sw.stateControl().setValue(false);
    }

	@Test
    public void accessModeChangedReportsCorrectPath() throws InterruptedException, BrokenBarrierException, TimeoutException {
        final OnOffSwitch sw = getApplicationManager().getResourceManagement().createResource(newResourceName(), OnOffSwitch.class);
        final OnOffSwitch sw2 = appMan2.getResourceManagement().createResource(newResourceName(), OnOffSwitch.class);

        final Resource targetResource = sw.settings().setpoint().create();
        sw2.settings().setAsReference(sw.settings());
        Resource sourceResource = sw2.settings().setpoint();

        final CyclicBarrier accessModeAvailableBarrier = new CyclicBarrier(2);
        final CountDownLatch accessModeUnavailableLatch = new CountDownLatch(1);

        AccessModeListener rdl = new AccessModeListener() {

            @Override
            public void accessModeChanged(Resource resource) {
                assertEquals(targetResource.getLocation(), resource.getLocation());
                assertEquals(targetResource.getPath(), resource.getPath());
                try {
                    if (resource.getAccessMode() == SHARED) {
                        accessModeAvailableBarrier.await();
                    } else {
                        accessModeUnavailableLatch.countDown();
                    }
                } catch (InterruptedException | BrokenBarrierException ex) {
                    fail(ex.toString());
                }
            }

        };

        sw.activate(true);
        // listeners are only notified if there is an actual access mode request.
        // losing the default shared access when there is no explicit request
        // will not trigger a callback.
        targetResource.addAccessModeListener(rdl);
        assertTrue(targetResource.requestAccessMode(SHARED, AccessPriority.PRIO_LOWEST));
        accessModeAvailableBarrier.await();
        assertEquals(SHARED, targetResource.getAccessMode());
        
        assertTrue(sourceResource.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_DEVICESPECIFIC));
        assertTrue(accessModeUnavailableLatch.await(5, TimeUnit.SECONDS));
        
        assertEquals(AccessPriority.PRIO_DEVICESPECIFIC, sourceResource.getAccessPriority());
        assertEquals(EXCLUSIVE, sourceResource.getAccessMode());
        assertEquals(READ_ONLY, targetResource.getAccessMode());
    }

	@Test
    public void accessModeChangedReportsCorrectPathReverse() throws InterruptedException, BrokenBarrierException, TimeoutException {
        final OnOffSwitch sw = getApplicationManager().getResourceManagement().createResource(newResourceName(), OnOffSwitch.class);
        final OnOffSwitch sw2 = appMan2.getResourceManagement().createResource(newResourceName(), OnOffSwitch.class);

        final Resource sourceResource = sw.settings().setpoint().create();
        final Resource targetResource = sw2.settings().setpoint();        


        final CyclicBarrier accessModeAvailableBarrier = new CyclicBarrier(2);
        final CountDownLatch accessModeUnavailableLatch = new CountDownLatch(1);

        AccessModeListener rdl = new AccessModeListener() {

            @Override
            public void accessModeChanged(Resource resource) {
                assertEquals(targetResource.getLocation(), resource.getLocation());
                assertEquals(targetResource.getPath(), resource.getPath());
                try {
                    if (resource.getAccessMode() == SHARED) {
                        accessModeAvailableBarrier.await();
                    } else {
                        accessModeUnavailableLatch.countDown();
                    }
                } catch (InterruptedException | BrokenBarrierException ex) {
                    fail(ex.toString());
                }
            }

        };

        sw.activate(true);
        // listeners are only notified if there is an actual access mode request.
        // losing the default shared access when there is no explicit request
        // will not trigger a callback.
        targetResource.addAccessModeListener(rdl);
        sw2.settings().setAsReference(sw.settings());
        assertTrue(targetResource.requestAccessMode(SHARED, AccessPriority.PRIO_LOWEST));
        accessModeAvailableBarrier.await(10, TimeUnit.SECONDS);
        assertEquals(SHARED, targetResource.getAccessMode());
        
        assertTrue(sourceResource.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_DEVICESPECIFIC));
        assertTrue(accessModeUnavailableLatch.await(5, TimeUnit.SECONDS));
        
        assertEquals(AccessPriority.PRIO_DEVICESPECIFIC, sourceResource.getAccessPriority());
        assertEquals(EXCLUSIVE, sourceResource.getAccessMode());
        assertEquals(READ_ONLY, targetResource.getAccessMode());
    }

	@Test
	public void accessModeWorksOnSchedules() {
		OnOffSwitch sw = getApplicationManager().getResourceManagement().createResource(newResourceName(),
				OnOffSwitch.class);
		final FloatResource value = sw.heatCapacity().create();
		final Schedule schedule = value.addDecorator("definition", Schedule.class);
		schedule.addValue(100, new FloatValue(10.f));
		schedule.setInterpolationMode(InterpolationMode.STEPS);
		sw.activate(true);

		schedule.requestAccessMode(AccessMode.SHARED, AccessPriority.PRIO_DEVICESPECIFIC);
		assertEquals(AccessPriority.PRIO_DEVICESPECIFIC, schedule.getAccessPriority());
		assertEquals(SHARED, schedule.getAccessMode());

		// Test: Another app gets exclusive write, original application goes back to read-only.
		assertNotNull(appMan2); // check that 2nd application exists.
		final Schedule schedule2 = (Schedule) appMan2.getResourceAccess().getResource(schedule.getPath());
		schedule2.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);

		assertEquals(AccessPriority.PRIO_HIGHEST, schedule2.getAccessPriority());
		assertEquals(EXCLUSIVE, schedule2.getAccessMode());
		assertEquals(READ_ONLY, schedule.getAccessMode());

		assertTrue(schedule2.addValue(12, new FloatValue(5.f)));
		assertFalse(schedule.addValue(1, new FloatValue(9.f)));
		assertEquals(schedule2.getValue(12).getValue().getFloatValue(), 5.f, 1.e-4);

		schedule2.requestAccessMode(READ_ONLY, AccessPriority.PRIO_LOWEST);
		assertEquals(SHARED, schedule.getAccessMode());
		assertEquals(SHARED, schedule2.getAccessMode());
	}
	
}
