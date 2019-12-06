/**
 * Copyright 2011-2019 Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
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
package org.ogema.tools.timeseries.v2.test;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.IntegerValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.core.timeseries.TimeSeries;
import org.ogema.tools.timeseries.v2.iterator.api.PeriodicIterator;
import org.ogema.tools.timeseries.v2.memory.PeriodicTimeSeries;
import org.ogema.tools.timeseries.v2.memory.TreeTimeSeries;
import org.ogema.tools.timeseries.v2.tools.TimeSeriesUtils;

public class PeriodicTimeSeriesTest {
	
	// for debugging
	private static void print(final Iterator<SampledValue> it) {
		while (it.hasNext()) {
			final SampledValue sv = it.next(); 
			System.out.println("   -- [" + sv.getTimestamp() + ": " + sv.getValue().getIntegerValue()+ "]") ;
		}
	}
	
	private static List<Value> getValues(int... values) {
		return Arrays.stream(values).mapToObj(IntegerValue::new).collect(Collectors.toList());
	}
	
	private static void assertSize(final Iterator<?> it, final int nrValues) {
		int cnt = 0;
		while (it.hasNext()) {
			it.next();
			cnt++;
		}
		Assert.assertEquals("Unexpected number of iterator values",nrValues, cnt);
	}
	
	private static void assertSampledValue(final long expectedTime, final int expectedValue, final SampledValue sv) {
		Assert.assertNotNull(sv);
		Assert.assertEquals(expectedTime, sv.getTimestamp());
		Assert.assertEquals(expectedValue, sv.getValue().getIntegerValue());
	}
	
	private static void assertSizeAndValues(final Iterator<SampledValue> it, final int nrValues, final int[] values, final int startIdx) {
		int cnt = 0;
		final int sz = values.length;
		int idx = startIdx;
		while (it.hasNext()) {
			final SampledValue sv = it.next();
			Assert.assertEquals("Unexpected iterator value at time " + sv.getTimestamp(), values[idx], sv.getValue().getIntegerValue()); 
			cnt++;
			idx = (idx + 1) % sz;
		}
		Assert.assertEquals("Unexpected number of iterator values",nrValues, cnt);
	}
	
	@Test
	public void periodicIteratorWorksWithSingleArg() {
		final int[] values0 = {3};
		final List<Value> values = getValues(values0);
		final PeriodicIterator it = new PeriodicIterator(values, 10, 40, 0, Duration.ofMillis(10), ZoneOffset.UTC);
		assertSizeAndValues(it, 4, values0, 0); // times 10,20,30,40
	}
	
	@Test
	public void periodicIteratorWorksWithSingleArgNoZone() {
		final int[] values0 = {3};
		final List<Value> values = getValues(values0);
		final PeriodicIterator it = new PeriodicIterator(values, 10, 40, 0, Duration.ofMillis(10), null);
		assertSizeAndValues(it, 4, values0, 0); // times 10,20,30,40
	}

	@Test
	public void periodicIteratorWorksWithSingleArg2() {
		final List<Value> values = getValues(3);
		final PeriodicIterator it = new PeriodicIterator(values, 10, 40, 5, Duration.ofMillis(10), ZoneOffset.UTC);
		assertSize(it, 3); // times 15,25,35
	}
	
	@Test
	public void periodicIteratorWorks() {
		final int[] values0 = {3,7};
		final List<Value> values = getValues(values0);
		final PeriodicIterator it = new PeriodicIterator(values, 10, 50, 0, Duration.ofMillis(10), ZoneOffset.UTC);
		assertSizeAndValues(it, 5, values0, 1); // times 10,20,30,40,50; first value is 7
	}
	
	@Test
	public void periodicIteratorWorksNoZone() {
		final int[] values0 = {3,7};
		final List<Value> values = getValues(values0);
		final PeriodicIterator it = new PeriodicIterator(values, 10, 50, 0, Duration.ofMillis(10), null);
		assertSizeAndValues(it, 5, values0, 1); // times 10,20,30,40,50; first value is 7
	}
	
	@Test
	public void periodicIteratorWorksWithDisplacement() {
		final int[] values0 = {3,7};
		final List<Value> values = getValues(values0);
		final PeriodicIterator it = new PeriodicIterator(values, 110, 150, 0, Duration.ofMillis(10), ZoneOffset.UTC);
		assertSizeAndValues(it, 5, values0, 1); // times 110,120,130,140,150; first value is 7
	}

	@Test
	public void timeseriesIteratorWorksWithDisplacement() {
		final int[] values0 = {3,7};
		final List<Value> values = getValues(values0);
		final Iterator<SampledValue> it = 
				new PeriodicTimeSeries(values, 0, null, Duration.ofMillis(10), ZoneOffset.UTC).iterator(110, 150);
		assertSizeAndValues(it, 5, values0, 1); // times 110,120,130,140,150; first value is 7
	}
	
	@Test
	public void timeseriesIteratorWorksWithDisplacement2() {
		final int[] values0 = {3,7};
		final List<Value> values = getValues(values0);
		final Iterator<SampledValue> it = 
				new PeriodicTimeSeries(values, 0, null, Duration.ofMillis(10), ZoneOffset.UTC).iterator(105, 155);
		assertSizeAndValues(it, 5, values0, 1); // times 110,120,130,140,150; first value is 7
	}
	
	@Test
	public void timeseriesIteratorWorksWithDisplacement3() {
		final int[] values0 = {3,7};
		final List<Value> values = getValues(values0);
		final Iterator<SampledValue> it = 
				new PeriodicTimeSeries(values, 0, null, Duration.ofMillis(10), ZoneOffset.UTC).getValues(110, 150).iterator();
		assertSizeAndValues(it, 5, values0, 1); // times 110,120,130,140,150; first value is 7
	}
	
	
	@Test
	public void getPreviousWorks() {
		final int[] values0 = {3,7};
		final List<Value> values = getValues(values0);
		final PeriodicTimeSeries t = new PeriodicTimeSeries(values, 0, null, Duration.ofMillis(10), ZoneOffset.UTC);
		assertSampledValue(0, 3, t.getPreviousValue(5));
		assertSampledValue(10, 7, t.getPreviousValue(15));
		assertSampledValue(-10, 7, t.getPreviousValue(-5));
		assertSampledValue(-20, 3, t.getPreviousValue(-20));
	}
	
	@Test
	public void getNextWorks() {
		final int[] values0 = {3,7};
		final List<Value> values = getValues(values0);
		final PeriodicTimeSeries t = new PeriodicTimeSeries(values, 0, null, Duration.ofMillis(10), ZoneOffset.UTC);
		assertSampledValue(10, 7, t.getNextValue(5));
		assertSampledValue(20, 3, t.getNextValue(15));
		assertSampledValue(0, 3, t.getNextValue(-5));
		assertSampledValue(-10, 7, t.getNextValue(-10));
	}
	
	@Test
	public void getValueWorks() {
		final int[] values0 = {3,7};
		final List<Value> values = getValues(values0);
		final PeriodicTimeSeries t = new PeriodicTimeSeries(values, 0, InterpolationMode.LINEAR, Duration.ofMillis(10), ZoneOffset.UTC);
		assertSampledValue(5, 5, t.getValue(5));
		assertSampledValue(10, 7, t.getValue(10));
		assertSampledValue(-10, 7, t.getValue(-10));
		assertSampledValue(0, 3, t.getValue(0));
		assertSampledValue(100, 3, t.getValue(100));
		assertSampledValue(105, 5, t.getValue(105));
	}
	
	@Test
	public void iteratorWorksDateBased() {
		final int[] values0 = {3};
		final LocalDateTime ldts = LocalDateTime.of(1970, 1, 1, 0, 0);
		final ZonedDateTime base = ZonedDateTime.of(ldts, ZoneOffset.UTC);
		final PeriodicIterator it = new PeriodicIterator(getValues(values0), base.plusYears(1).toInstant().toEpochMilli(), 
				base.plusYears(1).plusDays(2).toInstant().toEpochMilli(), 0, Period.ofDays(1), ZoneOffset.UTC);
		Assert.assertTrue(it.hasNext());
		Assert.assertEquals( base.plusYears(1).toInstant().toEpochMilli(), it.next().getTimestamp());
		Assert.assertEquals( base.plusYears(1).plusDays(1).toInstant().toEpochMilli(), it.next().getTimestamp());
		Assert.assertEquals( base.plusYears(1).plusDays(2).toInstant().toEpochMilli(), it.next().getTimestamp());
		Assert.assertFalse(it.hasNext());
	}
	
	@Test
	public void downsamplingWorks0() {
		final long oneDay = Duration.ofDays(1).toMillis();
		final List<SampledValue> values = Arrays.asList(
				new SampledValue(new FloatValue(1), oneDay, Quality.GOOD),
				new SampledValue(new FloatValue(3), 3 * oneDay, Quality.GOOD),
				new SampledValue(new FloatValue(7), 7 * oneDay, Quality.GOOD),
				new SampledValue(new FloatValue(20), 20 * oneDay, Quality.GOOD)
		);
		final TimeSeries ts = new TreeTimeSeries(null);
		ts.addValues(values);
		final Iterator<SampledValue> it = TimeSeriesUtils.downsample(ts.iterator(), 0, oneDay, InterpolationMode.LINEAR);
		int cnt = 0;
		while (it.hasNext()) {
			final SampledValue next =it.next();
			Assert.assertEquals(0.1, next.getTimestamp() / oneDay, next.getValue().getFloatValue());
			cnt++;
		}
		Assert.assertEquals("Unexpected number of iterator values", 20, cnt, 1); 
	}
	
	@Test
	public void downsamplingWorks1() {
		final long oneDay = Duration.ofDays(1).toMillis();
		final List<SampledValue> values = IntStream.of(1,3,7,20,145,201,202, 205, 221, 301, 370)
			.mapToObj(i -> new SampledValue(new FloatValue(i), i * oneDay, Quality.GOOD))
			.collect(Collectors.toList());
		final TimeSeries ts = new TreeTimeSeries(null);
		ts.addValues(values);
		final Iterator<SampledValue> it = TimeSeriesUtils.downsample(ts.iterator(), 0, 365 * oneDay, 
				Period.ofMonths(1), ZoneOffset.UTC, InterpolationMode.LINEAR);
		int cnt = 0;
		while (it.hasNext()) {
			final SampledValue next =it.next();
			Assert.assertEquals(0.1, next.getTimestamp() / oneDay, next.getValue().getFloatValue());
			cnt++;
			final LocalDateTime ldt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(next.getTimestamp()), ZoneOffset.UTC).toLocalDateTime();
			Assert.assertEquals(LocalTime.of(0, 0), ldt.toLocalTime());
		}
		Assert.assertEquals("Unexpected number of iterator values", 12, cnt, 1);  // we expect a value per month
	}
	
}
