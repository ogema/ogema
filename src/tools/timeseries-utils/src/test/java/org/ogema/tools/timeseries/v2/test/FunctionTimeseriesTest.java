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
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAmount;
import java.util.Iterator;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;
import org.ogema.core.channelmanager.measurements.IntegerValue;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.tools.timeseries.v2.memory.FunctionTimeSeries;

public class FunctionTimeseriesTest {

	private static final ZoneId zone = ZoneOffset.UTC;
	
	private static long toLong(final LocalDateTime ldt) {
		return ZonedDateTime.of(ldt, zone).toInstant().toEpochMilli();
	}
	
	private static LocalDateTime toLdt(final long t) {
		return ZonedDateTime.ofInstant(Instant.ofEpochMilli(t), zone).toLocalDateTime();
	}
	
	/**
	 * base time is always chosen equal to startTime
	 * @param start
	 * @param end
	 * @param function
	 * @return
	 */
	private static FunctionTimeSeries<IntegerValue> createFunction(
			final LocalDateTime start, 
			final LocalDateTime end, 
			final TemporalAmount interval,
			final Function<Long, Integer> function) {
		return new FunctionTimeSeries<IntegerValue>(t -> new IntegerValue(function.apply(t)), toLong(start), toLong(end), toLong(start), interval, zone);
	}
	
	@Test
	public void functionTimeseriesWorksHours() {
		final LocalDateTime start = LocalDateTime.of(1970, 1, 1, 0, 0);
		final LocalDateTime end = start.plusYears(1);
		final Function<Long, Integer> fct = t -> toLdt(t).getHour(); // returns the hour of day, i.e. a value between 0 and 23
		final FunctionTimeSeries<IntegerValue> function = createFunction(start, end, Duration.ofHours(1), fct);
		Assert.assertEquals("Unexpected number of hours per year", 1, 365 * 24, function.size());
		final SampledValue next0 = function.getNextValue(Long.MIN_VALUE);
		Assert.assertNotNull(next0);
		Assert.assertEquals(0, next0.getTimestamp());
		final Iterator<SampledValue> it = function.iterator();
		int cnt = 0;
		int max = Integer.MIN_VALUE;
		int min = Integer.MAX_VALUE;
		while (it.hasNext()) {
			final SampledValue sv = it.next();
			final int val = sv.getValue().getIntegerValue();
			if (val > max)
				max = val;
			if (val < min)
				min = val;
			cnt++;
		}
		Assert.assertEquals(1, 365 * 24, cnt);
		Assert.assertEquals(23, max);
		Assert.assertEquals(0, min);
	}
	
	@Test
	public void functionTimeseriesWorksMonths() {
		final LocalDateTime start = LocalDateTime.of(1970, 1, 1, 0, 0);
		final LocalDateTime end = start.plusYears(1);
		final Function<Long, Integer> fct = t -> toLdt(t).getMonth().getValue(); // returns the month of year, i.e. a value between 1 and 12
		final FunctionTimeSeries<IntegerValue> function = createFunction(start, end, Period.ofMonths(1), fct);
		Assert.assertEquals("Unexpected number of months per year", 1, 12, function.size());
		final SampledValue next0 = function.getNextValue(Long.MIN_VALUE);
		Assert.assertNotNull(next0);
		Assert.assertEquals(0, next0.getTimestamp());
		final Iterator<SampledValue> it = function.iterator();
		int cnt = 0;
		int max = Integer.MIN_VALUE;
		int min = Integer.MAX_VALUE;
		while (it.hasNext()) {
			final SampledValue sv = it.next();
			final int val = sv.getValue().getIntegerValue();
			if (val > max)
				max = val;
			if (val < min)
				min = val;
			cnt++;
		}
		Assert.assertEquals(1, 12, cnt);
		Assert.assertEquals(12, max);
		Assert.assertEquals(1, min);
	}
	
}
