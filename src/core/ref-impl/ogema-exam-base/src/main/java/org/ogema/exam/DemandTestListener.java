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
					expectedResource, resource), expectedResource, resource);
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
