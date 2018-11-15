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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import static org.junit.Assert.assertTrue;
import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.ResourceDemandListener;
import org.slf4j.LoggerFactory;

public class DemandTestListener<T extends Resource> implements ResourceDemandListener<T> {
	private T expectedResource;
	private volatile CountDownLatch available;
	private volatile CountDownLatch unavailable;

	public DemandTestListener() {
		reset();
	}

	public DemandTestListener(T expectedResource) {
		reset();
		this.expectedResource = expectedResource;
	}

	public void setExpectedResource(T r) {
		this.expectedResource = r;
	}

	public final void reset() {
		available = new CountDownLatch(1);
		unavailable = new CountDownLatch(1);
	}

	@Override
	public void resourceAvailable(T resource) {
		LoggerFactory.getLogger(getClass().getSimpleName()).info("resourceAvailable: {}", resource.getPath());
		if (expectedResource != null) {
			Assert.assertEquals(String.format("unexpected resource in resourceAvailable callback: %s != %s",
					expectedResource.getPath(), resource.getPath()), expectedResource, resource);
		}
		available.countDown();
	}

	@Override
	public void resourceUnavailable(T resource) {
		LoggerFactory.getLogger(getClass().getSimpleName()).info("resourceUnavailable {}", resource.getPath());
		if (expectedResource != null) {
			Assert.assertEquals(String.format("unexpected resource in resourceUnavailable callback: %s != %s",
					expectedResource, resource), expectedResource, resource);
		}
		unavailable.countDown();
	}

	public boolean awaitAvailable() throws InterruptedException {
		return available.await(5, TimeUnit.SECONDS);
	}

	public boolean awaitAvailable(long amount, TimeUnit unit) throws InterruptedException {
		return available.await(amount, unit);
	}

	public boolean awaitUnavailable() throws InterruptedException {
		return unavailable.await(5, TimeUnit.SECONDS);
	}

	public boolean awaitUnavailable(long amount, TimeUnit unit) throws InterruptedException {
		return unavailable.await(amount, unit);
	}

	public boolean availableCalled() {
		return available.getCount() == 0;
	}

	public boolean unavailableCalled() {
		return unavailable.getCount() == 0;
	}

	public void assertAvailable() throws InterruptedException {
		assertTrue(awaitAvailable());
	}

	public void assertUnavailable() throws InterruptedException {
		assertTrue(awaitUnavailable());
	}

}
