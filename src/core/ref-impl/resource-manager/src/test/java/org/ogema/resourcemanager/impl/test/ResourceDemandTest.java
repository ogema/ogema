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

import org.ogema.exam.DemandTestListener;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ogema.core.model.Resource;
import org.ogema.core.model.ValueResource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.resourcemanager.ResourceDemandListener;
import org.ogema.exam.TestApplication;
import org.ogema.model.actors.OnOffSwitch;
import org.ogema.model.devices.buildingtechnology.Thermostat;
import org.ogema.model.devices.whitegoods.CoolingDevice;
import org.ogema.model.locations.Room;
import org.ogema.model.sensors.GenericFloatSensor;
import org.ogema.model.sensors.Sensor;
import org.ogema.model.sensors.TemperatureSensor;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

/**
 * TODO: tests using optional elements, decorators and references (add & delete)
 *
 * @author jlapp
 */
@ExamReactorStrategy(PerClass.class)
public class ResourceDemandTest extends OsgiTestBase {

    private final Collection<String> newResources = new ArrayList<>();

    TestApplication appTwo = new TestApplication() {
    };

    @Before
    public void register2ndApp() throws InterruptedException {
        appTwo.registerAndAwaitStart(ctx);
    }

    @Override
    public String newResourceName() {
        String name = super.newResourceName();
        newResources.add(name);
        return name;
    }

    @After
    public void tearDown() {
        for (String resName : newResources) {
            Resource r = resAcc.getResource(resName);
            if (r != null) {
                r.delete();
            }
        }
        newResources.clear();
    }

    @Test
    public void existingResourcesAreReportedToNewResourceDemandListeners() throws Exception {
        final CountDownLatch availableCount = new CountDownLatch(3);

        ResourceDemandListener<Resource> l = new ResourceDemandListener<Resource>() {

            @Override
            public void resourceAvailable(Resource resource) {
                availableCount.countDown();
            }

            @Override
            public void resourceUnavailable(Resource resource) {
                fail("unexpected method call");
            }
        };

        OnOffSwitch sw1 = resMan.createResource(newResourceName(), OnOffSwitch.class);
        sw1.addDecorator("dummySwitch", OnOffSwitch.class);
        OnOffSwitch sw2 = resMan.createResource(newResourceName(), OnOffSwitch.class);
        sw1.activate(true);
        sw2.activate(true);
        for (Resource r: resAcc.getResources(OnOffSwitch.class)) {
            System.out.println(r.getPath());
        }
        assertEquals(3, resAcc.getResources(OnOffSwitch.class).size());

        resAcc.addResourceDemand(OnOffSwitch.class, l);
        assertTrue("available not called", availableCount.await(20, TimeUnit.SECONDS));
        sw1.delete();
        sw2.delete();
    }

    @Test
    public void onlyActiveResourcesAreReportedToListeners() throws InterruptedException {
        final Collection<Resource> reportedResources = new ArrayList<>();

        ResourceDemandListener<Resource> l = new ResourceDemandListener<Resource>() {

            @Override
            public void resourceAvailable(Resource resource) {
                reportedResources.add(resource);
            }

            @Override
            public void resourceUnavailable(Resource resource) {
                fail("unexpected method call");
            }
        };

        @SuppressWarnings("unused")
		OnOffSwitch sw1 = resMan.createResource(newResourceName(), OnOffSwitch.class);
        OnOffSwitch sw2 = resMan.createResource(newResourceName(), OnOffSwitch.class);
        sw2.activate(true);

        resAcc.addResourceDemand(OnOffSwitch.class, l);
        Thread.sleep(3000);
        assertTrue("no resources reported", reportedResources.size() >= 1);
        for (Resource r : reportedResources) {
            assertTrue("inactive resource reported", r.isActive());
        }
    }

    @Test
    public void newlyCreatedResourcesAreReportedToExistingResourceDemandListeners() throws Exception {
        final CountDownLatch availableCount = new CountDownLatch(1);

        ResourceDemandListener<Resource> l = new ResourceDemandListener<Resource>() {

            @Override
            public void resourceAvailable(Resource resource) {
                availableCount.countDown();
            }

            @Override
            public void resourceUnavailable(Resource resource) {
                fail("unexpected method call");
            }
        };

        resAcc.addResourceDemand(OnOffSwitch.class, l);
        OnOffSwitch sw = resMan.createResource(newResourceName(), OnOffSwitch.class);
        sw.activate(true);
        assertTrue("available not called", availableCount.await(20, TimeUnit.SECONDS));
    }

    @Test
    public void deactivatingAReportedResourceCausesResourceUnavailabeCallback() throws InterruptedException {
        DemandTestListener<OnOffSwitch> listener = new DemandTestListener<>();
        resAcc.addResourceDemand(OnOffSwitch.class, listener);
        OnOffSwitch res = resMan.createResource(newResourceName(), OnOffSwitch.class);
        listener.setExpectedResource(res);
        res.activate(true);
        assertTrue("available not called", listener.awaitAvailable());
        res.deactivate(true);
        assertTrue("unavailable not called", listener.awaitUnavailable());
    }

    /**
     * Checks the following scenario: <br>
     * 1) Resource is created and activated.<br>
     * 2) Demand Listener is registered.<br>
     * 3) Demand Listener receives callback on existing resource.<br>
     * 4) Resource is deactivated.<br>
     * 5) Test: resourceUnavailable callback is received.<br>
     * Test differs from
     * {@link #deactivatingAReportedResourceCausesResourceUnavailabeCallback()}
     * by the order of steps 1) and 2).
     *
     * @throws java.lang.InterruptedException
     */
    @Test
    public void unavailableCallbackReceivedOnPreviouslyExistingResource() throws InterruptedException {
        OnOffSwitch res = resMan.createResource(newResourceName(), OnOffSwitch.class);
        DemandTestListener<OnOffSwitch> l = new DemandTestListener<>(res);
        res.activate(true);
        resAcc.addResourceDemand(OnOffSwitch.class, l);
        assertTrue("available not called", l.awaitAvailable());
        res.deactivate(true);
        assertTrue("unavailable not called", l.awaitUnavailable());
    }

    @Test
    public void deletingAReportedResourceCausesResourceUnavailabeCallback() throws InterruptedException {
        DemandTestListener<OnOffSwitch> l = new DemandTestListener<>();
        resAcc.addResourceDemand(OnOffSwitch.class, l);

        OnOffSwitch res = resMan.createResource(newResourceName(), OnOffSwitch.class);
        res.activate(true);
        assertTrue("available not called", l.awaitAvailable());
        resMan.deleteResource(res.getName());
        assertTrue("unavailable not called", l.awaitUnavailable());
    }

    @Test
    public void unregisteredListenersReceiveNoCallbacks() throws InterruptedException {
        DemandTestListener<OnOffSwitch> l = new DemandTestListener<>();
        resAcc.addResourceDemand(OnOffSwitch.class, l);
        OnOffSwitch res = resMan.createResource(newResourceName(), OnOffSwitch.class);
        res.activate(true);
        assertTrue("available not called", l.awaitAvailable());
        resAcc.removeResourceDemand(OnOffSwitch.class, l);
        res.deactivate(true);
        assertFalse("should timeout", l.awaitUnavailable(3, TimeUnit.SECONDS));
    }

    @Test
    public void newlyCreatedNonReferenceOptionalElementsAreReportedToExistingResourceDemandListeners()
            throws InterruptedException {
        DemandTestListener<BooleanResource> l = new DemandTestListener<>();
        resAcc.addResourceDemand(BooleanResource.class, l);
        OnOffSwitch sw = resMan.createResource(newResourceName(), OnOffSwitch.class);
        l.setExpectedResource(sw.controllable());
        sw.controllable().create();
        assertFalse(sw.controllable().isActive());
        sw.controllable().activate(true);
        assertTrue("available not called", l.awaitAvailable());
    }

    @Test
    public void deletingCausesProperCallbacksWhen2AppsAreInvolved() throws InterruptedException {
        DemandTestListener<CoolingDevice> l = new DemandTestListener<>();
        resAcc.addResourceDemand(CoolingDevice.class, l);

        CoolingDevice cool1 = resMan.createResource(newResourceName(), CoolingDevice.class);
        l.setExpectedResource(cool1);

        cool1.activate(true);
        assertTrue(l.awaitAvailable());

        CoolingDevice coolApp2 = appTwo.getAppMan().getResourceAccess().getResource(cool1.getPath());
        assertEquals(cool1, coolApp2);

        coolApp2.delete();
        assertFalse(cool1.exists());
        assertFalse(coolApp2.exists());
        assertTrue(l.awaitUnavailable());
        resAcc.removeResourceDemand(CoolingDevice.class, l);
    }

    @Test
    public void listeningToGenericSensorsWorks() throws Exception {
        final CountDownLatch availableCount = new CountDownLatch(1);

        ResourceDemandListener<Sensor> l = new ResourceDemandListener<Sensor>() {

            @SuppressWarnings("unused")
			@Override
            public void resourceAvailable(Sensor sensor) {
                ValueResource reading = sensor.reading();
                /*
                 * explicit cast to super types to check correct class generation (synthetic methods)
                 */
                FloatResource fr = ((TemperatureSensor) sensor).reading();
                FloatResource fr2 = ((GenericFloatSensor) sensor).reading();
                if (!reading.isActive()) {
                    //XXX it's unspecified whether it should be active at this point
                    //fail("TemperatureSensor::reading was not active");
                }

                availableCount.countDown();
            }

            @Override
            public void resourceUnavailable(Sensor resource) {
                fail("unexpected method call");
            }
        };

        resAcc.addResourceDemand(Sensor.class, l);

        TemperatureSensor sensor = resMan.createResource(newResourceName(), TemperatureSensor.class);
        sensor.reading().create();

        sensor.activate(true);
        assertTrue("available not called", availableCount.await(20, TimeUnit.SECONDS));

        resAcc.removeResourceDemand(Sensor.class, l);
        sensor.delete();
    }

    private class RoomListener implements ResourceDemandListener<Room> {

        public volatile CountDownLatch foundLatch;
        public volatile CountDownLatch lostLatch;

        public volatile String lastAvailable = null;
		@SuppressWarnings("unused")
		public volatile String lastUnavailable = null;

        public RoomListener() {
            reset();
        }

        public void reset() {
            foundLatch = new CountDownLatch(1);
            lostLatch = new CountDownLatch(1);
        }

        @Override
        public void resourceAvailable(Room rm) {
            lastAvailable = rm.name().getValue();
//			System.out.println("  Available callback: " + lastAvailable);
            foundLatch.countDown();
        }

        @Override
        public void resourceUnavailable(Room rm) {
            lastUnavailable = rm.name().getValue();
//			System.out.println("  Unavailable callback: " + lastUnavailable);
			lostLatch.countDown();
		}
		
	};
	
    @Test
    public void deleteSubresourceAndRecreateImmediately() throws InterruptedException {
        Thermostat thermo = resMan.createResource(newResourceName(), Thermostat.class);
        DemandTestListener<Room> listener = new DemandTestListener<>();
        try {
            thermo.location().room().name().create();
            thermo.location().room().name().setValue("subRoom1");
            resAcc.addResourceDemand(Room.class, listener);
            listener.setExpectedResource(thermo.location().room());
            thermo.activate(true);
            assertTrue("must receive resource available callback", listener.awaitAvailable(5, TimeUnit.SECONDS));
            //assertEquals(subRoom, listener.lastAvailable);	
            listener.reset();
            thermo.location().room().delete();
            assertTrue("must receive resource unavailable callback", listener.awaitUnavailable());
        } finally {
            thermo.delete();
            resAcc.removeResourceDemand(Room.class, listener);
        }
    }
   	
    @Test
    public void deleteSubresourceAndSetAsReference() throws InterruptedException {
        String subRoom = "subRoom";
        String topRoom = "topRoom";
        Thermostat thermo = resMan.createResource(newResourceName(), Thermostat.class);
        Room room = resMan.createResource(topRoom, Room.class);
        RoomListener listener = new RoomListener();
        resAcc.addResourceDemand(Room.class, listener);
        try {
            listener.foundLatch.await(5, TimeUnit.SECONDS);
            listener.reset();
            thermo.location().room().name().create();
            thermo.location().room().name().setValue(subRoom);
            room.name().create();
            room.name().setValue(topRoom);
            thermo.activate(true);
            listener.foundLatch.await(5, TimeUnit.SECONDS);
            assertEquals(subRoom, listener.lastAvailable);
            listener.reset();
            room.activate(true);
            assertTrue(listener.foundLatch.await(5, TimeUnit.SECONDS));
            assertEquals(topRoom, listener.lastAvailable);
            thermo.location().room().setAsReference(room);
            assertTrue(listener.lostLatch.await(5, TimeUnit.SECONDS));
            // make sure the new reference is not set before the callback has been executed
            // ! actually this is not supported since resourceAvailable callbacks are asynchronous
            //assertEquals(subRoom, listener.lastUnavailable);
        } finally {
            thermo.delete();
            room.delete();
            resAcc.removeResourceDemand(Room.class, listener);
        }
    }

}
