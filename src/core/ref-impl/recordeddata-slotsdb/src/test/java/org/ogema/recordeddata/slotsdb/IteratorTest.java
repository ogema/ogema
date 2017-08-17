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
package org.ogema.recordeddata.slotsdb;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.recordeddata.RecordedDataConfiguration;
import org.ogema.core.recordeddata.RecordedDataConfiguration.StorageType;
import org.ogema.recordeddata.DataRecorderException;
import org.ogema.recordeddata.RecordedDataStorage;

public class IteratorTest extends DbTest {

	private static RecordedDataConfiguration config = new RecordedDataConfiguration();
	
	static {
		config.setStorageType(StorageType.ON_VALUE_UPDATE);
	}
	
	// requires timestamps to be ordered chronologically
	private static void assertIteratorWorks(RecordedDataStorage rds, long[] timestamps, Value[] values) throws DataRecorderException {
		addValues(rds, timestamps, values);
		Iterator<SampledValue> it = rds.iterator();
		int cnt = 0;
		SampledValue sv;
		while (it.hasNext()) {
			sv = it.next();
			Assert.assertEquals("Log data iterator returned sampled values in wrong order",timestamps[cnt++], sv.getTimestamp());
		}
		Assert.assertEquals("Iterator returns unexpected number of log data points",values.length, cnt);
	}
	
	@Test 
	public void emptyIterationWorks() throws DataRecorderException {
		RecordedDataStorage rds = sdb.createRecordedDataStorage("emptyIteratorConfig", config);
		assertIteratorWorks(rds, new long[0], new Value[0]);
	}
	
	@Test 
	public void singlePointIterationWorks() throws DataRecorderException {
		RecordedDataStorage rds = sdb.createRecordedDataStorage("singlePointIteratorConfig", config);
		long[] t = new long[] { System.currentTimeMillis() };
		float[] f = new float[] { 34.2F };
		assertIteratorWorks(rds, t, createValues(f));
	}
	
	@Test
	public void singleDayIterationWorks() throws DataRecorderException {
		// we must use different configurations to avoid interference between the tests
		RecordedDataStorage rds = sdb.createRecordedDataStorage("singleDayIteratorConfig", config);
		long[] t = new long[] { ONE_DAY / 10, 2 * ONE_DAY / 10,  5 * ONE_DAY / 10, 7 * ONE_DAY / 10};
		float[] f = new float[] {23, -123.3F, 2344, 0};
		assertIteratorWorks(rds, t, createValues(f));
	}
	
	@Test 
	public void twoDaysIterationWorksBasic() throws DataRecorderException {
		RecordedDataStorage rds = sdb.createRecordedDataStorage("twoDaysBasicIteratorConfig", config);
		long[] t = new long[] { 3 * ONE_DAY / 10, 17 * ONE_DAY / 10};
		float[] f = new float[] {23, 2344};
		assertIteratorWorks(rds, t, createValues(f));
	}
	
	@Test 
	public void twoConsecutiveDaysIterationWorks() throws DataRecorderException {
		RecordedDataStorage rds = sdb.createRecordedDataStorage("twoConsecutiveDaysIteratorConfig", config);
		long[] t = new long[] { ONE_DAY / 10, 2 * ONE_DAY / 10,  5 * ONE_DAY / 10, 7 * ONE_DAY / 10, 
					ONE_DAY, 11* ONE_DAY/10, 16* ONE_DAY / 10, 19 * ONE_DAY / 10};
		float[] f = new float[] {23, -123.3F, 234, 4 , -7.1F, 34, 19923.234F, 65};
		assertIteratorWorks(rds, t, createValues(f));
	}
	
	@Test
	public void twoSeparatedDaysIterationWorks() throws DataRecorderException {
		long offset = 600 * ONE_DAY;
		RecordedDataStorage rds = sdb.createRecordedDataStorage("twoSeparatedDaysIteratorConfig", config);
		long[] t = new long[] { ONE_DAY / 10, 2 * ONE_DAY / 10,  5 * ONE_DAY / 10, 7 * ONE_DAY / 10,  
					offset + ONE_DAY/10, offset + 6* ONE_DAY / 10, offset + 9 * ONE_DAY / 10};
		float[] f = new float[] {23, -123.3F, 234, 4 , -7.1F, 19923.234F, 7};
		assertIteratorWorks(rds, t, createValues(f));
		
	}
	
	// first two days in a row, then one day break, another day filled, three days break, two days filled
	@Test
	public void multipleDaysIterationWorks() throws DataRecorderException {
		RecordedDataStorage rds = sdb.createRecordedDataStorage("multidDaysIteratorConfig", config);
		long[] t = new long[] { ONE_DAY / 10, 2 * ONE_DAY / 10,  5 * ONE_DAY / 10, 7 * ONE_DAY / 10,  
					11 * ONE_DAY/10, 13 * ONE_DAY / 10, 18 * ONE_DAY / 10,
					32 * ONE_DAY/10, 33 * ONE_DAY / 10, 39 * ONE_DAY/10,
					74 * ONE_DAY/10,
					81 * ONE_DAY/10, 82 * ONE_DAY/10, 83* ONE_DAY/10, 84* ONE_DAY/10, 90 * ONE_DAY/10
					};
		float[] f = new float[] {23, -123.3F, 234, 4 , 
					-7.1F, 19923.234F, 3,
					324, 34.5F, -12.2F,
					324234.23F,
					1, 2, 3, 4 ,5};
		assertIteratorWorks(rds, t, createValues(f));
	}
	
	/**
	 * Tests if caching works correctly (if implemented)
	 * @throws DataRecorderException
	 */
	@Test
	public void singleDayIterationWorksTwice() throws DataRecorderException {
		// we must use different configurations to avoid interference between the tests
		RecordedDataStorage rds = sdb.createRecordedDataStorage("singleDayIteratorConfig2", config);
		long[] t = new long[] { ONE_DAY / 10, 2 * ONE_DAY / 10,  5 * ONE_DAY / 10, 7 * ONE_DAY / 10};
		float[] f = new float[] {23, -123.3F, 2344, 0};
		final Value[] values = createValues(f);
		assertIteratorWorks(rds, t, values);
		final Iterator<SampledValue> it = rds.iterator();
		int cnt = 0;
		SampledValue sv;
		while (it.hasNext()) {
			sv = it.next();
			Assert.assertEquals("Log data iterator returned sampled values in wrong order",t[cnt++], sv.getTimestamp());
		}
		Assert.assertEquals("Iterator returns unexpected number of log data points",values.length, cnt);
	}
	
	/**
	 * Tests if caching works correctly (if implemented)
	 * @throws DataRecorderException
	 */
	@Test
	public void multipleDaysIterationWorksTwice() throws DataRecorderException {
		RecordedDataStorage rds = sdb.createRecordedDataStorage("multidDaysIteratorConfig2", config);
		long[] t = new long[] { ONE_DAY / 10, 2 * ONE_DAY / 10,  5 * ONE_DAY / 10, 7 * ONE_DAY / 10,  
					11 * ONE_DAY/10, 13 * ONE_DAY / 10, 18 * ONE_DAY / 10,
					32 * ONE_DAY/10, 33 * ONE_DAY / 10, 39 * ONE_DAY/10,
					74 * ONE_DAY/10,
					81 * ONE_DAY/10, 82 * ONE_DAY/10, 83* ONE_DAY/10, 84* ONE_DAY/10, 90 * ONE_DAY/10
					};
		float[] f = new float[] {23, -123.3F, 234, 4 , 
					-7.1F, 19923.234F, 3,
					324, 34.5F, -12.2F,
					324234.23F,
					1, 2, 3, 4 ,5};
		final Value[] values = createValues(f);
		assertIteratorWorks(rds, t, values);
		final Iterator<SampledValue> it = rds.iterator();
		int cnt = 0;
		SampledValue sv;
		while (it.hasNext()) {
			sv = it.next();
			Assert.assertEquals("Log data iterator returned sampled values in wrong order",t[cnt++], sv.getTimestamp());
		}
		Assert.assertEquals("Iterator returns unexpected number of log data points",values.length, cnt);
	}
	
	// 30 days in a row with a lot of data, then a gap, then some more points
	@Test 
	public void largeDataSetIterationWorks() throws DataRecorderException {
		RecordedDataStorage rds = sdb.createRecordedDataStorage("muchDataIteratorConfig", config);
		List<Long> t = new ArrayList<>();
		List<Float> f = new ArrayList<>();
		for (int d=0;d<30;d++) {
			generateDummyData(d* ONE_DAY, t, f);
		}
		for (int d=300;d<305;d++) {
			generateDummyData(d*ONE_DAY, t, f);
		}
		long[] t1 = new long[t.size()];
		float[] f1 = new float[t.size()];
		for (int i=0;i<t.size();i++) {
			t1[i] = t.get(i);
			f1[i] = f.get(i);
		}
		assertIteratorWorks(rds, t1, createValues(f1));
	}
	
	private static void generateDummyData(long offset, List<Long> t , List<Float> f) {
		long t0 = offset;
		float f0;
		for (int i=0;i<1000;i++) {
			f0 = (float) Math.random();
			t0 +=(long) (f0 * 60000) + 1;
			t.add(t0);
			f.add(f0);
		}
	}
	
	/**
	 * Two parallel threads, one iterates over the time series and counts the points, meanwhile, the other one 
	 * adds a few points.
	 * @throws DataRecorderException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException 
	 */
	@Test
	public void logDataIteratorIsFailSafe() throws DataRecorderException, InterruptedException, ExecutionException, TimeoutException {
		final RecordedDataStorage rds = sdb.createRecordedDataStorage("logDataIteratorIsFailSafeConfig", config);
		List<Long> t = new ArrayList<>();
		List<Float> f = new ArrayList<>();
		for (int d=0;d<30;d++) {
			generateDummyData(d* ONE_DAY, t, f);
		}
		for (int d=300;d<305;d++) {
			generateDummyData(d*ONE_DAY, t, f);
		}
		long[] t1 = new long[t.size()];
		float[] f1 = new float[t.size()];
		for (int i=0;i<t.size();i++) {
			t1[i] = t.get(i);
			f1[i] = f.get(i);
		}
		addValues(rds, t1, createValues(f1));
		final int initialSize = rds.size();
		final CountDownLatch startLatch = new CountDownLatch(1);
		final CountDownLatch iteratorLatch = new CountDownLatch(1);
		final Callable<Integer> getNrDataPoints = new Callable<Integer>() {

			@Override
			public Integer call() throws Exception {
				Assert.assertTrue(startLatch.await(10, TimeUnit.SECONDS));	
				iteratorLatch.countDown();
				Iterator<SampledValue> it = rds.iterator();
				int cnt = 0;
				while (it.hasNext()) {
					it.next();
					cnt++;
					Thread.sleep(0); // ensure the task doesn't run too fast for the concurrent modification to take place at all
				}
				return cnt;
			}
		};
		final Callable<Void> modifier = new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				startLatch.countDown();
				Assert.assertTrue(iteratorLatch.await(10, TimeUnit.SECONDS));
				for (int i=0;i<100;i++) {
					rds.insertValue(new SampledValue(new FloatValue(17.2F), 400*ONE_DAY + i * ONE_DAY/1000, Quality.GOOD));
					Thread.sleep(1);
				}
				return null;
			}
		};
		ExecutorService exec = Executors.newFixedThreadPool(2);
		Future<Integer> counterResult = exec.submit(getNrDataPoints);
		Future<Void> modifierResult = exec.submit(modifier);
		Integer result = counterResult.get(1,TimeUnit.MINUTES);
		modifierResult.get(1,TimeUnit.MINUTES);
		System.out.println(" Found " + result + " log data points from iterator, expected at least " + initialSize);
		Assert.assertTrue("Unexpectedly small number of log data points. Exepected > " + initialSize + ", got " + result,
				result > initialSize);
		
	}
	
	@Test 
	public void hasNextIsIdempotent() throws DataRecorderException {
		RecordedDataStorage rds = sdb.createRecordedDataStorage("storage", config);
		long[] t = new long[] { 3 * ONE_DAY / 10, 17 * ONE_DAY / 10 , 18 * ONE_DAY / 10, 19 * ONE_DAY / 10};
		float[] f = new float[] {23, 2344, 7, 2};
		addValues(rds, t, createValues(f));
		Iterator<SampledValue> it = rds.iterator();
		for (int i=0;i<4;i++) {
			for (int j=0;j<5;j++) 
				Assert.assertTrue("Iterator#hasNext should return true", it.hasNext());
			it.next();
		}
		for (int j=0;j<5;j++) 
			Assert.assertFalse("Iterator#hasNext should return false", it.hasNext());
		sdb.deleteRecordedDataStorage(rds.getPath());
	}
}
