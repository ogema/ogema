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
package org.ogema.resourcemanager.impl.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Test;
import org.ogema.core.model.simple.IntegerResource;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

/**
 * Test atomic read/write operations
 */
@ExamReactorStrategy(PerClass.class)
public class AtomicReadWriteTest extends OsgiTestBase {

	private static List<Future<Integer>> parallelGetAndIncrement(final int threads, final IntegerResource resource, final boolean addOrSet) throws InterruptedException {
		final CountDownLatch appLatch = new CountDownLatch(threads);
		final CountDownLatch adminLatch = new CountDownLatch(1);
		final ExecutorService exec = Executors.newFixedThreadPool(threads);
		final List<Future<Integer>> results=  new ArrayList<>();
		for (int i=0; i<threads; i++) {
			if (addOrSet)
				results.add(exec.submit(new GetAndIncrementOperation(resource, appLatch, adminLatch)));
			else
				results.add(exec.submit(new GetAndSetOperation(resource, appLatch, adminLatch, i+1)));
		}
		Assert.assertTrue("App tasks not ready...", appLatch.await(10, TimeUnit.SECONDS));
		adminLatch.countDown();
		exec.shutdown();
		return results;
	}
	
	private static void getAndAddWorks(final int threads, final IntegerResource resource, final boolean addOrSet) throws InterruptedException, ExecutionException, TimeoutException {
		resource.setValue(0);
		final List<Future<Integer>> result = parallelGetAndIncrement(threads, resource, addOrSet);
		final Set<Integer> values = new HashSet<>();
		for (Future<Integer> f : result) {
			Assert.assertTrue("Value contained twice, seems that getAndAdd is not atomic.", values.add(f.get(5, TimeUnit.SECONDS)));
		}
		if (addOrSet)
			Assert.assertEquals("Unexpected maximum value " + Collections.max(values) 
				+ " as result of " + threads + " getAndAdd(1) operations.", threads-1, (int) Collections.max(values));
		else
			Assert.assertTrue("Unexpected maximum value " + Collections.max(values) 
				+ " as result of " + threads + " getAndSet operations.", threads >= (int) Collections.max(values));
	}
	
	@Test
	public void atomicGetAndAddWorks() throws InterruptedException, ExecutionException, TimeoutException {
		final IntegerResource resource = getApplicationManager().getResourceManagement().createResource(newResourceName(), IntegerResource.class);
		getAndAddWorks(150, resource, true);
	}
	
	@Test
	public void atomicGetAndSetWorks() throws InterruptedException, ExecutionException, TimeoutException {
		final IntegerResource resource = getApplicationManager().getResourceManagement().createResource(newResourceName(), IntegerResource.class);
		getAndAddWorks(150, resource, false);
	}
	
	private static class GetAndIncrementOperation implements Callable<Integer> {
		
		private final IntegerResource resource;
		private final CountDownLatch appLatch;
		private final CountDownLatch adminLatch;
		
		public GetAndIncrementOperation(IntegerResource resource, CountDownLatch appLatch, CountDownLatch adminLatch) {
			this.resource = resource;
			this.appLatch = appLatch;
			this.adminLatch = adminLatch;
		}

		@Override
		public Integer call() throws Exception {
			// wait until all apps are ready to execute their task
			appLatch.countDown();
			Assert.assertTrue(adminLatch.await(30, TimeUnit.SECONDS));
			final int val = resource.getAndAdd(1);
			
			return val;
		}
		
	}
	
	private static class GetAndSetOperation implements Callable<Integer> {
		
		private final IntegerResource resource;
		private final CountDownLatch appLatch;
		private final CountDownLatch adminLatch;
		private final int value;
		
		public GetAndSetOperation(IntegerResource resource, CountDownLatch appLatch, CountDownLatch adminLatch, int value) {
			this.resource = resource;
			this.appLatch = appLatch;
			this.adminLatch = adminLatch;
			this.value = value;
		}

		@Override
		public Integer call() throws Exception {
			// wait until all apps are ready to execute their task
			appLatch.countDown();
			Assert.assertTrue(adminLatch.await(30, TimeUnit.SECONDS));
			return resource.getAndSet(value);
		}
		
	}

}
