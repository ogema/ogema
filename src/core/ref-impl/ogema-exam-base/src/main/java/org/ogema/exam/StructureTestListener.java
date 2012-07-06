/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ogema.exam;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.ResourceStructureEvent;
import org.ogema.core.resourcemanager.ResourceStructureListener;

/**
 *
 * @author jlapp
 */
public class StructureTestListener implements ResourceStructureListener {
    final Map<ResourceStructureEvent.EventType, CountDownLatch> eventLatches = new EnumMap<>(ResourceStructureEvent.EventType.class);
    Resource expectedSource;
    Resource expectedChangedResource;
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
        System.out.printf("%s: %s, %s%n", event.getType(), event.getSource(), event.getChangedResource());
        if (expectedSource != null) {
            Assert.assertEquals("wrong event source", expectedSource, event.getSource());
        }
        if (expectedChangedResource != null) {
            Assert.assertEquals("wrong changed resource", expectedChangedResource, event.getChangedResource());
        }
        eventLatches.get(event.getType()).countDown();
    }

    public void reset() {
        for (ResourceStructureEvent.EventType e : ResourceStructureEvent.EventType.values()) {
            eventLatches.put(e, new CountDownLatch(1));
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
    
}
