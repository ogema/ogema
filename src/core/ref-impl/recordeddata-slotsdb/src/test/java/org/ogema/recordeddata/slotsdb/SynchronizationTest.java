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
package org.ogema.recordeddata.slotsdb;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.recordeddata.RecordedDataConfiguration;
import org.ogema.core.recordeddata.RecordedDataConfiguration.StorageType;
import org.ogema.recordeddata.DataRecorderException;
import org.ogema.recordeddata.RecordedDataStorage;

public class SynchronizationTest extends SlotsDbTest {
	
	private static final int SLOTS_PER_DAY = 1000;
	private final static boolean ignore = "true".equalsIgnoreCase(System.getenv("NO_LONG_TESTS")) || Boolean.getBoolean("NO_LONG_TESTS");
	
	private static RecordedDataConfiguration getConfig(int i) {
		RecordedDataConfiguration config= new RecordedDataConfiguration();
		switch (i % 3) {
		case 0:
			config.setFixedInterval(ONE_DAY/SLOTS_PER_DAY);
			config.setStorageType(StorageType.FIXED_INTERVAL);
			break;
		case 1:
			config.setStorageType(StorageType.ON_VALUE_CHANGED);
			break;
		case 2:
			config.setStorageType(StorageType.ON_VALUE_UPDATE);
			break;
		default:
			throw new IllegalStateException();
		}
		return config;
	}
	
	/**
	 * Generate log data for several configurations in the same time interval, and in parallel access the already stored data in
	 * a reader thread. -> Verify that SlotsDb synchronization works properly, both for single config synchronization and 
	 * daily folder access synchronization. 
	 * @param flushPeriod
	 * @throws Throwable 
	 */
	private void multipleLogsWorkInParallel(int nrItems, long flushPeriod) throws Throwable {
		deleteTestFiles();
		System.setProperty(SlotsDb.class.getPackage().getName().toLowerCase() + ".flushperiod", flushPeriod/1000+""); // flush every X seconds
		final SlotsDb sdb = new SlotsDb(SlotsDb.DB_TEST_ROOT_FOLDER);
		try {
			final CountDownLatch initLatch = new CountDownLatch(2 * nrItems);
			final CountDownLatch startLatch = new CountDownLatch(1);
			final ExecutorService exec = Executors.newFixedThreadPool(2 * nrItems);
			final List<Future<Integer>> results  =new ArrayList<>();
			RecordedDataStorage rds;
			AtomicBoolean done;
			for (int i = 0;i<nrItems; i++) {
				rds = sdb.createRecordedDataStorage("synchroTestConfig_" + (nrItems + flushPeriod) + i, getConfig(i));
				done = new AtomicBoolean(false);
				SlotsLogger logger = new SlotsLogger(rds, initLatch, startLatch, done);
				results.add(exec.submit(logger));
				SlotsAnalyzer analyzer = new SlotsAnalyzer(rds, initLatch, startLatch, done, flushPeriod);
				results.add(exec.submit(analyzer));
			}
			Assert.assertTrue("Logger threads not started",initLatch.await(30, TimeUnit.SECONDS));
			startLatch.countDown();
			Thread.sleep(10000);
			Integer expected = null;
			for (Future<Integer> result: results) {
				int next;
				try {
					next = result.get(1, TimeUnit.MINUTES);
				} catch (ExecutionException e) {
					throw e.getCause();
				}
				if (expected == null) {
					expected = next;
					continue;
				}
				Assert.assertEquals("Tasks returning different log data sizes", expected.intValue(), next);
			}
		} finally {
			sdb.deactivate(null);
			deleteTestFiles();
			System.setProperty(SlotsDb.class.getPackage().getName().toLowerCase() + ".flushperiod", "0");
		}
	}
	
	@Test
	public void multipleLogsWorkInParallelImmediateFlush() throws Throwable {
		Assume.assumeFalse(ignore);
		// works with 50 as well, but may occasionally cause a FileNotFoundException (Too many open files)
		final int nrItems = 30;
		multipleLogsWorkInParallel(nrItems, 0);
	}
	
	@Test
	public void multipleLogsWorkInParallelDelayedFlush() throws Throwable {
		Assume.assumeFalse(ignore);
		// works with 50 as well, but may occasionally cause a FileNotFoundException (Too many open files)
		final int nrItems = 30;
		multipleLogsWorkInParallel(nrItems, 5000);
	}
	
	@Test
	public void twoLogsWorkInParallelDelayedFlush() throws Throwable {
		multipleLogsWorkInParallel(2, 5000);
	}
	
	
	public static class SlotsLogger implements Callable<Integer> {
		
		private final CountDownLatch initLatch;
		private final CountDownLatch startLatch;
		private final RecordedDataStorage rds;
		private final AtomicBoolean done;
		
		public SlotsLogger(RecordedDataStorage rds,CountDownLatch initLatch,CountDownLatch startLatch,AtomicBoolean done) {
			this.rds = rds;
			this.initLatch = initLatch;
			this.startLatch = startLatch;
			this.done = done;
		}
		
		private int generateOneDayData(long offset) throws DataRecorderException, InterruptedException {
			for (int i=0; i< SLOTS_PER_DAY; i++) {
				rds.insertValue(new SampledValue(new FloatValue((float) Math.random()) , offset + i * ONE_DAY/SLOTS_PER_DAY, Quality.GOOD));
				Thread.sleep(0);
			}
			return SLOTS_PER_DAY;
		}

		@Override
		public Integer call() throws Exception {
			initLatch.countDown();
			Assert.assertTrue(startLatch.await(30, TimeUnit.SECONDS));
			int cnt = 0;
			for (int d=0; d<5; d++) {
				cnt += generateOneDayData(d * ONE_DAY);
			}
			for (int d=100;d<103;d++) {
				cnt += generateOneDayData(d * ONE_DAY);
			}
			done.set(true);
			return cnt;
		}
		
	}

	public static class SlotsAnalyzer implements Callable<Integer> {
		
		private final CountDownLatch initLatch;
		private final CountDownLatch startLatch;
		private final RecordedDataStorage rds;
		private final AtomicBoolean done;
		private final long flushPeriod;
		// make this volatile to ensure that the loop in call is not optimized away
		public volatile int sz;
		
		public SlotsAnalyzer(RecordedDataStorage rds,CountDownLatch initLatch,CountDownLatch startLatch,AtomicBoolean done, long flushPeriod) {
			this.rds = rds;
			this.initLatch = initLatch;
			this.startLatch = startLatch;
			this.done = done;
			this.flushPeriod = flushPeriod;
		}

		@Override
		public Integer call() throws Exception {
			initLatch.countDown();
			Assert.assertTrue(startLatch.await(30, TimeUnit.SECONDS));
			while (!done.get()) {
				sz = rds.getValues(Long.MIN_VALUE).size();
				sz = rds.size();
				Thread.sleep(1);
			}
			if (flushPeriod > 0)
				Thread.sleep(flushPeriod + 2000); // if data is flushed only periodically, we need to wait a bit longer before evaluating the result
			sz = rds.size();
			Assert.assertEquals("Size function returns wrong value.", rds.getValues(Long.MIN_VALUE).size(),sz);
			return sz;
		}
		
	}
	

}
