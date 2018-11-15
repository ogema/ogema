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

import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.recordeddata.RecordedDataConfiguration;
import org.ogema.core.recordeddata.RecordedDataConfiguration.StorageType;
import org.ogema.recordeddata.DataRecorderException;
import org.ogema.recordeddata.RecordedDataStorage;

public class SizeTest extends DbTest {
	
	private final static long FIXED_STEP = 10000;
	private static final RecordedDataConfiguration configUpdate = new RecordedDataConfiguration();
	private static final RecordedDataConfiguration configFixed = new RecordedDataConfiguration();
	
	static {
		configUpdate.setStorageType(StorageType.ON_VALUE_UPDATE);
		configFixed.setFixedInterval(FIXED_STEP);
		configFixed.setStorageType(StorageType.FIXED_INTERVAL);
	}
	
	@Test
	public void emptyLogdataWorks() throws DataRecorderException {
		RecordedDataStorage rds1 = sdb.createRecordedDataStorage("emptySizeTestStorage1", configUpdate);
		RecordedDataStorage rds2 = sdb.createRecordedDataStorage("emptySizeTestStorage2", configFixed);
		Assert.assertTrue(rds1.isEmpty());
		Assert.assertTrue(rds2.isEmpty());
		Assert.assertEquals(0, rds1.size());
		Assert.assertEquals(0, rds2.size());
		Assert.assertTrue(rds1.isEmpty(123, System.currentTimeMillis()));
		Assert.assertTrue(rds2.isEmpty(123, System.currentTimeMillis()));
		Assert.assertEquals(0, rds1.size(123, System.currentTimeMillis()));
		Assert.assertEquals(0, rds2.size(123, System.currentTimeMillis()));
	}

	@Test
	public void singlePointWorks() throws DataRecorderException {
		RecordedDataStorage rds1 = sdb.createRecordedDataStorage("singlePointSizeTestStorage1", configUpdate);
		RecordedDataStorage rds2 = sdb.createRecordedDataStorage("singlePointSizeTestStorage2", configFixed);
		rds1.insertValue(new SampledValue(new FloatValue(324.2F), 2*ONE_DAY/10, Quality.GOOD));
		rds2.insertValue(new SampledValue(new FloatValue(324.2F), 2*ONE_DAY/10, Quality.GOOD));
		Assert.assertFalse(rds1.isEmpty());
		Assert.assertFalse(rds2.isEmpty());
		Assert.assertEquals(1, rds1.size());
		Assert.assertEquals(1, rds2.size());
		
		Assert.assertFalse(rds1.isEmpty(Long.MIN_VALUE,Long.MAX_VALUE));
		Assert.assertFalse(rds2.isEmpty(Long.MIN_VALUE, Long.MAX_VALUE));
		Assert.assertEquals(1, rds1.size(Long.MIN_VALUE, Long.MAX_VALUE));
		Assert.assertEquals(1, rds2.size(Long.MIN_VALUE, Long.MAX_VALUE));
		
		Assert.assertFalse(rds1.isEmpty(ONE_DAY/10, 3*ONE_DAY/10));
		Assert.assertFalse(rds2.isEmpty(ONE_DAY/10, 3*ONE_DAY/10));
		Assert.assertEquals(1, rds1.size(ONE_DAY/10, 3*ONE_DAY/10));
		Assert.assertEquals(1, rds2.size(ONE_DAY/10, 3*ONE_DAY/10));
		
		Assert.assertTrue(rds1.isEmpty(Long.MIN_VALUE, ONE_DAY/10));
		Assert.assertTrue(rds2.isEmpty(Long.MIN_VALUE, ONE_DAY/10));
		Assert.assertEquals(0, rds1.size(Long.MIN_VALUE, ONE_DAY/10));
		Assert.assertEquals(0, rds2.size(Long.MIN_VALUE, ONE_DAY/10));
		
		Assert.assertTrue(rds1.isEmpty(3*ONE_DAY/10, Long.MAX_VALUE));
		Assert.assertTrue(rds2.isEmpty(3*ONE_DAY/10, Long.MAX_VALUE));
		Assert.assertEquals(0, rds1.size(3*ONE_DAY/10, Long.MAX_VALUE));
		Assert.assertEquals(0, rds2.size(3*ONE_DAY/10, Long.MAX_VALUE));

	}
	
	@Test
	public void multiplePointsOneDayWorks() throws DataRecorderException {
		final int nr = 50;
		RecordedDataConfiguration configFixed = new RecordedDataConfiguration();
		configFixed.setStorageType(StorageType.FIXED_INTERVAL);
		configFixed.setFixedInterval(ONE_DAY/nr);
		
		RecordedDataStorage rds1 = sdb.createRecordedDataStorage("multiplePointsOneDaySizeTestStorage1", configUpdate);
		RecordedDataStorage rds2 = sdb.createRecordedDataStorage("multiplePointsOneDaySizeTestStorage2", configFixed);

		for (int i=0;i<nr;i++) {
			rds1.insertValue(new SampledValue(new FloatValue((float) Math.random()), i*ONE_DAY/nr, Quality.GOOD));
			rds2.insertValue(new SampledValue(new FloatValue((float) Math.random()), i*ONE_DAY/nr, Quality.GOOD));
		}
		Assert.assertFalse(rds1.isEmpty());
		Assert.assertFalse(rds2.isEmpty());
		Assert.assertEquals(nr, rds1.size());
		Assert.assertEquals(nr, rds2.size());
		
		Assert.assertFalse(rds1.isEmpty(0, ONE_DAY));
		Assert.assertFalse(rds2.isEmpty(0, ONE_DAY));
		Assert.assertEquals(nr, rds1.size(0, ONE_DAY));
		Assert.assertEquals(nr, rds2.size(0, ONE_DAY));
		
	}
	
	@Test
	public void twoDaysWorks() throws DataRecorderException, InterruptedException {
		RecordedDataConfiguration configFixed = new RecordedDataConfiguration();
		configFixed.setStorageType(StorageType.FIXED_INTERVAL);
		configFixed.setFixedInterval(ONE_DAY/2);
		
		RecordedDataStorage rds1 = sdb.createRecordedDataStorage("twoDaysSizeTestStorage1", configUpdate);
		RecordedDataStorage rds2 = sdb.createRecordedDataStorage("twoDaysSizeTestStorage2", configFixed);

		rds1.insertValue(new SampledValue(new FloatValue((float) Math.random()), ONE_DAY/2, Quality.GOOD));
		rds2.insertValue(new SampledValue(new FloatValue((float) Math.random()), ONE_DAY/2, Quality.GOOD));
		rds1.insertValue(new SampledValue(new FloatValue((float) Math.random()), 3*ONE_DAY/2, Quality.GOOD));
		rds2.insertValue(new SampledValue(new FloatValue((float) Math.random()), 3*ONE_DAY/2, Quality.GOOD));
		
		Assert.assertFalse(rds1.isEmpty());
		Assert.assertFalse(rds2.isEmpty());
		Assert.assertEquals(2, rds1.size());
		Assert.assertEquals(2, rds2.size());
		
		Assert.assertFalse(rds1.isEmpty(0, ONE_DAY));
		Assert.assertFalse(rds2.isEmpty(0, ONE_DAY));
		Assert.assertEquals(1, rds1.size(0, ONE_DAY));
		Assert.assertEquals(1, rds2.size(0, ONE_DAY));

		Assert.assertTrue(rds1.isEmpty(3*ONE_DAY/4, 5*ONE_DAY/4));
		Assert.assertTrue(rds2.isEmpty(3*ONE_DAY/4, 5*ONE_DAY/4));
		Assert.assertEquals(0, rds1.size(3*ONE_DAY/4, 5*ONE_DAY/4));
		Assert.assertEquals(0, rds2.size(3*ONE_DAY/4, 5*ONE_DAY/4));
		
	}
	
	@Test
	public void severalDaysWork() throws DataRecorderException, InterruptedException {
		final int nr = 50;
		RecordedDataConfiguration configFixed = new RecordedDataConfiguration();
		configFixed.setStorageType(StorageType.FIXED_INTERVAL);
		configFixed.setFixedInterval(ONE_DAY/nr);
		
		RecordedDataStorage rds1 = sdb.createRecordedDataStorage("severalDaysSizeTestStorage1", configUpdate);
		RecordedDataStorage rds2 = sdb.createRecordedDataStorage("severalDaysSizeTestStorage2", configFixed);
		
		int[] days = new int[]{0,1,2,5,6,123};
		int cnt = 0;
		
		for (int day: days) {
			for (int i=0;i<nr;i++) {
				cnt++;
				rds1.insertValue(new SampledValue(new FloatValue((float) Math.random()), day * ONE_DAY + i*ONE_DAY/nr, Quality.GOOD));
				rds2.insertValue(new SampledValue(new FloatValue((float) Math.random()), day * ONE_DAY + i*ONE_DAY/nr, Quality.GOOD));
			}
		}

		Assert.assertFalse(rds1.isEmpty());
		Assert.assertFalse(rds2.isEmpty());
		Assert.assertEquals(cnt, rds1.size());
		Assert.assertEquals(cnt, rds2.size());
		
		Assert.assertFalse(rds1.isEmpty(0, ONE_DAY));
		Assert.assertFalse(rds2.isEmpty(0, ONE_DAY));

		Assert.assertTrue(rds1.isEmpty(8*ONE_DAY, 15*ONE_DAY));
		Assert.assertTrue(rds2.isEmpty(8*ONE_DAY, 15*ONE_DAY));
		
	}
	
	@Test 
	public void separationWorks() throws DataRecorderException {
		final String id = "test/resource";
		sdb.deleteRecordedDataStorage(id);
		final RecordedDataStorage rds = sdb.createRecordedDataStorage(id, configUpdate);
		Assert.assertTrue(rds.isEmpty());
		rds.insertValue(new SampledValue(new FloatValue(1), 1, Quality.GOOD));
		rds.insertValue(new SampledValue(new FloatValue(2), 101, Quality.GOOD));
		long nextPeriod = 10 * 60 * 60 * 1000;
		rds.insertValue(new SampledValue(new FloatValue(11), nextPeriod+1, Quality.GOOD));
		rds.insertValue(new SampledValue(new FloatValue(12), nextPeriod + 100, Quality.GOOD));

		Assert.assertEquals("Size function failure", 2, rds.size(0, nextPeriod));
		Assert.assertEquals("Size function failure", 2, rds.size(nextPeriod, Long.MAX_VALUE));
		
		Assert.assertEquals("Wrong iterator size", 2, size(rds.iterator(0, nextPeriod)));
		Assert.assertEquals("Wrong iterator size", 2, size(rds.iterator(nextPeriod, Long.MAX_VALUE)));
	}
	
	@Test
	public void separationWorks2() throws DataRecorderException {
		final String id = "test/resource1";
		sdb.deleteRecordedDataStorage(id);
		final RecordedDataStorage rds = sdb.createRecordedDataStorage(id, configUpdate);
		Assert.assertTrue(rds.isEmpty());
		rds.insertValue(new SampledValue(new FloatValue(1), 1, Quality.GOOD));
		rds.insertValue(new SampledValue(new FloatValue(2), 101, Quality.GOOD));
		long nextDay = 26 * 60 * 60 * 1000;
		rds.insertValue(new SampledValue(new FloatValue(11), nextDay+1, Quality.GOOD));
		rds.insertValue(new SampledValue(new FloatValue(12), nextDay + 100, Quality.GOOD));

		Assert.assertEquals("Size function failure", 2, rds.size(0, nextDay));
		Assert.assertEquals("Size function failure", 2, rds.size(nextDay, Long.MAX_VALUE));
		
		Assert.assertEquals("Wrong iterator size", 2, size(rds.iterator(0, nextDay)));
		Assert.assertEquals("Wrong iterator size", 2, size(rds.iterator(nextDay, Long.MAX_VALUE)));
	}
	
	static int size(Iterator<?> it) {
		int cnt = 0;
		while (it.hasNext()) {
			it.next();
			cnt++;
			
		}
		return cnt;
	}
	
	
}
