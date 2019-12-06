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
package org.ogema.tools.timeseries.v2.test;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;
import org.ogema.core.channelmanager.measurements.DoubleValue;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.core.timeseries.ReadOnlyTimeSeries;
import org.ogema.core.timeseries.TimeSeries;
import org.ogema.tools.timeseries.v2.iterator.api.MultiTimeSeriesIterator;
import org.ogema.tools.timeseries.v2.iterator.api.MultiTimeSeriesIteratorBuilder;
import org.ogema.tools.timeseries.v2.memory.TreeTimeSeries;
import org.ogema.tools.timeseries.v2.tools.TimeSeriesUtils;

public class ValueTest {
	
	@Test
	public void integrationWorks1() {
		final TimeSeries t = new TreeTimeSeries(InterpolationMode.LINEAR);
		t.addValue(0, FloatValue.ZERO);
		t.addValue(10, new FloatValue(10));
		t.addValue(11, FloatValue.ZERO);
		t.addValue(21, new FloatValue(10));
		final double d = TimeSeriesUtils.integrate(t);
		Assert.assertEquals("Unexpected integration result", 105, d, 1);
	}
	
	@Test
	public void integrationWorks2() {
		final TimeSeries t = new TreeTimeSeries(InterpolationMode.STEPS);
		t.addValue(0, new FloatValue(10));
		t.addValue(10, FloatValue.ZERO);
		t.addValue(15, new FloatValue(10));
		t.addValue(20, FloatValue.ZERO);
		final double d = TimeSeriesUtils.integrate(t);
		Assert.assertEquals("Unexpected integration result", 150, d, 1);
	}
	
	@Test
	public void integration2Works3() {
		final TreeTimeSeries t = new TreeTimeSeries(InterpolationMode.LINEAR);
		final double value = 10;
		final Value v = new DoubleValue(value);
		t.addValue(0, v);
		t.addValue(15, v);
		t.addValue(30, v);
		t.addValue(45, v);
		t.addValue(60, v);
		assertAverageMatches(t, 0, 5, value);
		assertAverageMatches(t, 0, 10, value);
		assertAverageMatches(t, 0, 15, value);
		assertAverageMatches(t, 0, 20, value);
		assertAverageMatches(t, 0, 25, value);
		assertAverageMatches(t, 0, 30, value);
		assertAverageMatches(t, 0, 35, value);
		assertAverageMatches(t, 15, 20, value);
		assertAverageMatches(t, 15, 25, value);
		assertAverageMatches(t, 15, 30, value);
		assertAverageMatches(t, 15, 35, value);
		assertAverageMatches(t, 15, 40, value);
		assertAverageMatches(t, 15, 45, value);
		assertAverageMatches(t, 15, 50, value);
		assertAverageMatches(t, 15, 60, value);
	}

	@Test
	public void boundaryValuesWorkInMultiIterator() {
		boundaryValuesWorkInMultiIterator(InterpolationMode.LINEAR, false);
	}
	
	@Test
	public void boundaryValuesWorkInMultiIterator2() {
		boundaryValuesWorkInMultiIterator(InterpolationMode.STEPS, false);
	}
	
	@Test
	public void boundaryValuesWorkInMultiIterator3() {
		boundaryValuesWorkInMultiIterator(InterpolationMode.LINEAR, true);
	}
	
	@Test
	public void boundaryValuesWorkInMultiIterator4() {
		boundaryValuesWorkInMultiIterator(InterpolationMode.STEPS, true);
	}
	
	private void boundaryValuesWorkInMultiIterator(final InterpolationMode mode, final boolean doIntegrate) {
		final TreeTimeSeries t = new TreeTimeSeries(mode);
		final double value = 10;
		final Value v = new DoubleValue(value);
		t.addValue(0, v);
		t.addValue(15, v);
		t.addValue(30, v);
		t.addValue(45, v);
		assertMonotonicallyIncreasingTime(t, 0, 5, doIntegrate);
		assertMonotonicallyIncreasingTime(t, 0, 10, doIntegrate);
		assertMonotonicallyIncreasingTime(t, 0, 15, doIntegrate);
		assertMonotonicallyIncreasingTime(t, 0, 20, doIntegrate);
		assertMonotonicallyIncreasingTime(t, 0, 30, doIntegrate);
		assertMonotonicallyIncreasingTime(t, 0, 40, doIntegrate);
		assertMonotonicallyIncreasingTime(t, 0, 45, doIntegrate);
		assertMonotonicallyIncreasingTime(t, 15, 20, doIntegrate);
		assertMonotonicallyIncreasingTime(t, 15, 30, doIntegrate);
		assertMonotonicallyIncreasingTime(t, 15, 40, doIntegrate);
		assertMonotonicallyIncreasingTime(t, 15, 45, doIntegrate);
	}
	
	private static void assertMonotonicallyIncreasingTime(final ReadOnlyTimeSeries t, final long start, final long end, final boolean doIntegrate) {
		final SampledValue lowerBoundary = t.isEmpty(start, start) ? t.getValue(start) : null;
		final SampledValue upperBoundary = t.isEmpty(end, end) ? t.getValue(end) : null;
		final MultiTimeSeriesIterator multiI = getMultiIterator(t, start, end, lowerBoundary, upperBoundary, doIntegrate);
		long lastT = Long.MIN_VALUE;
		while (multiI.hasNext()) {
			final SampledValue svdp = multiI.next().getElement(0);
			Assert.assertTrue("Iterator timeseries steps not increasing: " + lastT + ": " + svdp.getTimestamp(), svdp.getTimestamp() > lastT);
			lastT = svdp.getTimestamp();
		}
	}
	
	private static MultiTimeSeriesIterator getMultiIterator(final ReadOnlyTimeSeries t, long start, long end, 
			SampledValue lowerBoundary, SampledValue upperBoundary, final boolean doIntegrate) {
		final MultiTimeSeriesIteratorBuilder builder = MultiTimeSeriesIteratorBuilder.newBuilder(Collections.singletonList(t.iterator(start, end)))
				.setGlobalInterpolationMode(t.getInterpolationMode())
				.doIntegrate(doIntegrate);
		if (lowerBoundary != null)
			builder.setLowerBoundaryValues(Collections.singletonMap(0, lowerBoundary));
		if (upperBoundary != null)
			builder.setUpperBoundaryValues(Collections.singletonMap(0, upperBoundary));
		return builder.build();
	}
	
	private static void assertAverageMatches(final ReadOnlyTimeSeries ts, final long start, final long end, final double expected) {
		Assert.assertEquals("Unexpected average value of timeseries " + ts + " over interval " + start + " - " + end, expected, getAverage(ts, start, end), 0.001);
		Assert.assertEquals("Unexpected average value of timeseries " + ts + " over interval " + start + " - " + end, expected, TimeSeriesUtils.getAverage(ts, start, end), 0.001);
	}
	
	private static double getAverage(final ReadOnlyTimeSeries ts, final long start, final long end) {
		final double integral = TimeSeriesUtils.integrate(ts, start, end);
		return integral / (end - start);
	}

}
