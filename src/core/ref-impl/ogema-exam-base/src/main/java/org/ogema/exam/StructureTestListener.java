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
package org.ogema.exam;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.ResourceStructureEvent;
import org.ogema.core.resourcemanager.ResourceStructureEvent.EventType;
import org.ogema.core.resourcemanager.ResourceStructureListener;

/**
 *
 * @author jlapp
 */
public class StructureTestListener implements ResourceStructureListener {
    protected final Map<ResourceStructureEvent.EventType, CountDownLatch> eventLatches =
            Collections.synchronizedMap(new EnumMap<EventType, CountDownLatch>(ResourceStructureEvent.EventType.class));
    protected final Map<ResourceStructureEvent.EventType, AtomicInteger> eventCounts =
            Collections.synchronizedMap(new EnumMap<EventType, AtomicInteger>(ResourceStructureEvent.EventType.class));
    protected volatile Resource expectedSource;
    protected volatile Resource expectedChangedResource;
    protected volatile ResourceStructureEvent lastEvent;
    {
        reset();
    }

    public void setExpectedSource(Resource expectedSource) {
        this.expectedSource = expectedSource;
    }

    public void setExpectedChangedResource(Resource expectedChangedResource) {
        this.expectedChangedResource = expectedChangedResource;
    }

    @Override
    public void resourceStructureChanged(ResourceStructureEvent event) {
        //printf might cause deadlock
        System.out.println(String.format("%s: %s, %s", event.getType(), event.getSource(), event.getChangedResource()));
        lastEvent = event;
        EventType type = event.getType();
        if (expectedSource != null) {
            Assert.assertEquals("wrong event source", expectedSource, event.getSource());
        }
        if (expectedChangedResource != null) {
            Assert.assertEquals("wrong changed resource", expectedChangedResource, event.getChangedResource());
        }
        synchronized (eventCounts) {
            eventCounts.get(type).incrementAndGet();
        	//eventCounts.put(type, eventCounts.get(type)+1);
        }
        eventLatches.get(type).countDown();
    }

    public void reset() {
        reset(1);
    }
    
    public void reset(int nrExpectedEvents) {
        for (ResourceStructureEvent.EventType e : ResourceStructureEvent.EventType.values()) {
        	eventCounts.put(e, new AtomicInteger(0));
            eventLatches.put(e, new CountDownLatch(nrExpectedEvents));
        }
    }

    public boolean awaitActivate(long amount, TimeUnit unit) throws InterruptedException {
        return eventLatches.get(ResourceStructureEvent.EventType.RESOURCE_ACTIVATED).await(amount, unit);
    }

    public boolean awaitCreate(long amount, TimeUnit unit) throws InterruptedException {
        return eventLatches.get(ResourceStructureEvent.EventType.RESOURCE_CREATED).await(amount, unit);
    }

    public boolean awaitReferenceAdded(long amount, TimeUnit unit) throws InterruptedException {
        return eventLatches.get(ResourceStructureEvent.EventType.REFERENCE_ADDED).await(amount, unit);
    }

    public boolean awaitReferenceRemoved(long amount, TimeUnit unit) throws InterruptedException {
        return eventLatches.get(ResourceStructureEvent.EventType.REFERENCE_REMOVED).await(amount, unit);
    }

    public boolean awaitEvent(ResourceStructureEvent.EventType type, long amount, TimeUnit unit) throws InterruptedException {
        return eventLatches.get(type).await(amount, unit);
    }

    public boolean awaitEvent(ResourceStructureEvent.EventType type) throws InterruptedException {
        return eventLatches.get(type).await(5, TimeUnit.SECONDS);
    }

    public boolean eventReceived(ResourceStructureEvent.EventType type) {
        return eventLatches.get(type).getCount() == 0;
    }
    
    public ResourceStructureEvent getLastEvent() {
        return lastEvent;
    }
    
    public int getEventCount(EventType type) {
        synchronized (eventCounts) {
            return eventCounts.get(type).get();
        }
    }
    
}
