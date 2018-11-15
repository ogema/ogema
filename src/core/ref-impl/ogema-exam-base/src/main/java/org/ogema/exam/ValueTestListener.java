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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.ValueResource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.resourcemanager.ResourceValueListener;

/**
 * Throws some AssertionErrors in the resourceChanged method; in order to make use of them 
 * in the test thread, register an {@link ExceptionHandler} there and call its 
 * {@link ExceptionHandler#checkForExceptionsInOtherThreads()} method at
 * the end of the test.
 * 
 * @author cnoelle
 *
 * @param <T>
 */
public class ValueTestListener<T extends ValueResource> implements ResourceValueListener<T> {

	private volatile CountDownLatch cdl = new CountDownLatch(1);
	private volatile int nrCallbacks = 0;
	private volatile T lastSource = null;
	private final List<T> expectedSources = new ArrayList<T>();
	private volatile String expectedValue = null;
	private volatile String lastValue = null;
	private final ExceptionHandler handler;
	
	public ValueTestListener(ApplicationManager am) {
		handler = new ExceptionHandler();
		am.addExceptionListener(handler);
	}
	
	@Override
	public synchronized void resourceChanged(T resource) {
		System.out.printf(" ooo Value changed: %s%n", resource.getPath());
		try {
			if (!expectedSources.isEmpty()) 
				if (expectedSources.size() == 1)
					Assert.assertEquals("Value changed callback from unexpected source", expectedSources.get(0), resource);
				else 
					Assert.assertTrue("Value changed callback from unexpected source", expectedSources.contains(resource));
			if (resource instanceof SingleValueResource) {
				lastValue = getValue((SingleValueResource) resource);
				if (expectedValue != null)
					Assert.assertEquals("Unexpected value", expectedValue, lastValue);
			}
			else
				lastValue = null;
		} catch (AssertionError e) {
			handler.exceptionOccured(e);
		}
		expectedValue = null;
		lastSource = resource;
		nrCallbacks++;
		cdl.countDown(); // -> count down is executed in any case -> tests may fail to fail if they do not keep track of exceptions in the listener thread
	}
	
	/**
	 * @throws AssertionError
	 * 		if and only if an AssertionError occured in the listener thread
	 */
	public void checkForExceptionsInListenerThread() throws AssertionError {
		handler.checkForExceptionsInOtherThreads();
	}
	
	public void reset() {
		reset(1);
	}
	
	public synchronized void reset(int nrExpectedCallbacks) {
		cdl = new CountDownLatch(nrExpectedCallbacks);
		nrCallbacks = 0;
	}
	
	public synchronized void setExpectedSource(T resource) {
		expectedSources.clear();
		if (resource != null)
			expectedSources.add(resource);
	}
	
	public synchronized void setExpectedSources(Collection<T> resources) {
		expectedSources.clear();
		if (resources != null)
			expectedSources.addAll(resources);
	}
	
	public synchronized void addExpectedSource(T resource) {
		expectedSources.add(resource);
	}
	
	public T getLastSource() {
		return lastSource;
	}
	
	public int getNrCallbacks() {
		return nrCallbacks;
	}
	
	public void setExpectedValue(String value) {
		this.expectedValue = value;
	}
	
	public String getLastValue() {
		return lastValue;
	}
	
	public boolean await() throws InterruptedException {
		return cdl.await(5, TimeUnit.SECONDS);
	}
	
	public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
		return cdl.await(timeout, unit);
	}
	
	public void assertCallback() throws InterruptedException {
		Assert.assertTrue("Callback missing", await());
	}
	
	public static String getValue(SingleValueResource resource) {
		if (resource instanceof StringResource) {
			return ((StringResource) resource).getValue();
		}
		else if (resource instanceof FloatResource) {
			return String.valueOf(((FloatResource) resource).getValue());
		}
		else if (resource instanceof IntegerResource) {
			return String.valueOf(((IntegerResource) resource).getValue());
		}
		else if (resource instanceof BooleanResource) {
			return String.valueOf(((BooleanResource) resource).getValue());
		}
		else if (resource instanceof TimeResource) {
			return String.valueOf(((TimeResource) resource).getValue());
		}
		else
			throw new RuntimeException();
	}
}
