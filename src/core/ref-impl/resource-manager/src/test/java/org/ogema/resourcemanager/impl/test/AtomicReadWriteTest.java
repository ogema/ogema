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
