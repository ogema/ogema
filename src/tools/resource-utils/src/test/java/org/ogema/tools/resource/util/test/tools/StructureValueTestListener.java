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
package org.ogema.tools.resource.util.test.tools;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.ogema.core.model.Resource;
import org.ogema.core.model.ValueResource;
import org.ogema.core.resourcemanager.ResourceStructureEvent;
import org.ogema.core.resourcemanager.ResourceStructureEvent.EventType;
import org.ogema.tools.listener.util.StructureValueListener;

public class StructureValueTestListener<T extends ValueResource> implements StructureValueListener<T> {

	public static final String VALUE_CHANGED_ID = "VALUE_CHANGED";
	private volatile CountDownLatch cdl = new CountDownLatch(1);
	private volatile Resource lastSource = null;
	private volatile Resource expectedSource = null;
	private volatile String expectedType = null; // use EventType name() or "VALUE_CHANGED" for value changes
	private final Resource source;
	
	public StructureValueTestListener(Resource source) {
		this.source = source;
	}
	
	@Override
	public void resourceChanged(T resource) {
		System.out.printf(" ooo Value changed: %s%n", resource.getPath());
		if (expectedType != null)
			Assert.assertEquals("Unexpected value changed callback", expectedType, VALUE_CHANGED_ID);
		if (expectedSource != null) 
			Assert.assertEquals("Value changed callback from unexpected source", expectedSource, resource);
		expectedSource = null;
		expectedType = null;
		lastSource = resource;
		cdl.countDown();
	}
	
	@Override
	public void resourceStructureChanged(ResourceStructureEvent event) {
		System.out.printf(" ooo structure change of type %s; source: %s, changed: %s%n", event.getType().name(), event.getSource().getLocation(), event.getChangedResource().getLocation());
		Assert.assertEquals("Structure change callback with unexpected source", source, event.getSource());
		if (expectedType != null)
			Assert.assertEquals("Structure change callback of unexpected type", expectedType, event.getType().name());
		Resource changed = event.getChangedResource();
		if (expectedSource != null) 
			Assert.assertEquals("Structure change callback from unexpected source", expectedSource, changed);
		expectedSource = null;
		lastSource = changed;
		cdl.countDown();
	}
	
	public void reset() {
		cdl = new CountDownLatch(1);
	}
	
	public void setExpectedSource(Resource resource) {
		this.expectedSource = resource;
	}
	
	/**
	 * Type must be either the name of an {@link EventType} enum, or
	 * equal to {@link StructureValueTestListener#VALUE_CHANGED_ID}, or
	 * null.
	 * @param type
	 */
	public void setExpectedType(String type) {
		this.expectedType = type;
	}
	
	public Resource getLastSource() {
		return lastSource;
	}
	
	public boolean await() throws InterruptedException {
		return cdl.await(5, TimeUnit.SECONDS);
	}
	
	public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
		return cdl.await(timeout, unit);
	}

}
