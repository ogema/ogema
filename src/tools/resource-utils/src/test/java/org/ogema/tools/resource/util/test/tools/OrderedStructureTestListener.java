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
package org.ogema.tools.resource.util.test.tools;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.ResourceStructureEvent;
import org.ogema.exam.StructureTestListener;

public class OrderedStructureTestListener extends StructureTestListener {

	private final Queue<Resource> expectedChangedResources = new LinkedList<Resource>();
	private volatile CountDownLatch cdl = new CountDownLatch(0);
	private final ApplicationManager am;
	
	public OrderedStructureTestListener(ApplicationManager am) {
		this.am = am;
	}
	
	@Override
	public synchronized void resourceStructureChanged(ResourceStructureEvent event) {
		Resource expected = expectedChangedResources.poll();
		super.setExpectedChangedResource(expected); // ok if null
		System.out.printf("%s: %s, %s%n", event.getType(), event.getSource(), event.getChangedResource());
        lastEvent = event;
        try {
	        if (expectedSource != null) {
	            Assert.assertEquals("wrong event source", expectedSource, event.getSource());
	        }
	        if (expectedChangedResource != null) {
	            Assert.assertEquals("wrong changed resource", expectedChangedResource, event.getChangedResource());
	        }
        } catch (AssertionError e) {
        	am.reportException(e);
        }
        eventLatches.get(event.getType()).countDown();
		cdl.countDown();
	}
	
	public synchronized void addExpectedChangedResource(Resource resource) {
		expectedChangedResources.offer(resource);
		cdl = new CountDownLatch(expectedChangedResources.size());
	}
	
	@Override
	public synchronized void reset() {
		super.reset();
		if (expectedChangedResources != null) // required since this is called in constructor of super class
			expectedChangedResources.clear();
		if (cdl != null)
			cdl = new CountDownLatch(0);
	}
	
	public boolean awaitQueue(long time, TimeUnit unit) throws InterruptedException {
		return cdl.await(time, unit);
	}
	
}
