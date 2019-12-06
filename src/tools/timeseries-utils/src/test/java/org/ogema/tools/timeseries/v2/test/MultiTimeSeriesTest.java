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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.core.timeseries.ReadOnlyTimeSeries;
import org.ogema.core.timeseries.TimeSeries;
import org.ogema.tools.timeseries.v2.iterator.api.MultiTimeSeriesIterator;
import org.ogema.tools.timeseries.v2.iterator.api.MultiTimeSeriesIteratorBuilder;
import org.ogema.tools.timeseries.v2.iterator.api.SampledValueDataPoint;
import org.ogema.tools.timeseries.v2.memory.TreeTimeSeries;
import org.ogema.tools.timeseries.v2.tools.SampleTimeSeries;
import org.ogema.tools.timeseries.v2.tools.TimeSeriesUtils;

public class MultiTimeSeriesTest {
	
	@Test
	public void singleTimeSeriesIteratorWorks() {
		final TimeSeries t = new TreeTimeSeries(null);
		t.addValues(SampleTimeSeries.createRandomTimeSeries(10, 0, 10, true));
		final MultiTimeSeriesIterator iterator = MultiTimeSeriesIteratorBuilder.newBuilder(Collections.singletonList(t.iterator())).build(); 
		int cnt = 0;
		while (iterator.hasNext()) {
			final SampledValueDataPoint sv = iterator.next();
			Assert.assertNotNull(sv);
			final long time = sv.getTimestamp();
			Assert.assertFalse("MultiIterator for a single time series returned an additional point. ", t.isEmpty(time, time));
			final SampledValue previous = sv.previous(0);
			final SampledValue next = sv.next(0);
			switch (cnt) {
			case 0:
				Assert.assertNull(previous);
				Assert.assertNotNull(next);
				break;
			case 9: 
				Assert.assertNotNull(previous);
				Assert.assertNull(next);
				break;
			default:
				Assert.assertNotNull(previous);
				Assert.assertNotNull(next);
			}
			cnt++;
		}
		Assert.assertEquals("MultiIterator for a single time series returned an unexpected number of points. ",t.size(), cnt);
	}

	@Test
	public void singleTimeSeriesIteratorWorksForRestrictedInterval() {
		final TimeSeries t = new TreeTimeSeries(null);
		t.addValues(SampleTimeSeries.createRandomTimeSeries(10, 0, 10, true));
		final long start = 30;
		final long end = 70;
		final MultiTimeSeriesIterator iterator = MultiTimeSeriesIteratorBuilder.newBuilder(Collections.singletonList(t.iterator(start,end))).build();
		int cnt = 0;
		while (iterator.hasNext()) {
			final SampledValueDataPoint sv = iterator.next();
			Assert.assertNotNull(sv);
			final long time = sv.getTimestamp();
			Assert.assertFalse("MultiIterator for a single time series returned an additional point. ", t.isEmpty(time, time));
			cnt++;
		}
		Assert.assertEquals("MultiIterator for a single time series returned an unexpected number of points. ",t.size(start,end), cnt);
	}
	
	@Test
	public void singleTimeSeriesIteratorWorksWithBoundaryPoints() {
		final TimeSeries t = new TreeTimeSeries(InterpolationMode.STEPS);
		t.addValues(SampleTimeSeries.createRandomTimeSeries(10, 0, 10, false));
		final long start = 25;
		final long end = 65;
		final MultiTimeSeriesIterator iterator = TimeSeriesUtils.getMultiIterator(Collections.<ReadOnlyTimeSeries> singletonList(t), start, end);
		int cnt = 0;
		while (iterator.hasNext()) {
			final SampledValueDataPoint sv = iterator.next();
			Assert.assertNotNull(sv);
			cnt++;
		}
		// here we expect two boundary points to be added
		Assert.assertEquals("MultiIterator for a single time series returned an unexpected number of points. ", t.size(start, end) + 2, cnt);
	}
    
	
	@Test
	public void twoTimeSeriesIteratorWorksBasic() {
		// two time series with 2 points each, one of them overlapping
		final TimeSeries t0 = new TreeTimeSeries(null);
		t0.addValues(SampleTimeSeries.createRandomTimeSeries(2, 0, 10, false));
		final TimeSeries t1 = new TreeTimeSeries(null);
		t1.addValues(SampleTimeSeries.createRandomTimeSeries(2, 10, 10, false));
		final MultiTimeSeriesIterator iterator = MultiTimeSeriesIteratorBuilder.newBuilder(
				Arrays.asList(t0.iterator(), t1.iterator())).build();
		assertPointMatches(iterator, new int[]{0});
		assertPointMatches(iterator, new int[]{0,1});
		assertPointMatches(iterator, new int[]{1});
		Assert.assertFalse(iterator.hasNext());
	}
	
	@Test
	public void threeTimeSeriesIteratorWorksBasic() {
		// three time series with three points each, some of them overlapping
		final TimeSeries t0 = new TreeTimeSeries(null);
		t0.addValues(SampleTimeSeries.createRandomTimeSeries(3, 0, 10, false));
		final TimeSeries t1 = new TreeTimeSeries(null);
		t1.addValues(SampleTimeSeries.createRandomTimeSeries(3, 10, 10, false));
		final TimeSeries t2= new TreeTimeSeries(null);
		t2.addValues(SampleTimeSeries.createRandomTimeSeries(3, 20, 10, false));
		final MultiTimeSeriesIterator iterator =  MultiTimeSeriesIteratorBuilder.newBuilder(
				Arrays.asList(t0.iterator(), t1.iterator(), t2.iterator())).build();
		assertPointMatches(iterator, new int[]{0});
		assertPointMatches(iterator, new int[]{0,1});
		assertPointMatches(iterator, new int[]{0,1,2});
		assertPointMatches(iterator, new int[]{1,2});
		assertPointMatches(iterator, new int[]{2});
		Assert.assertFalse(iterator.hasNext());
	}
	
	@Test
	public void historicalDataStorageWorksForSingleTimeSeries() {
		final TimeSeries t = new TreeTimeSeries(null);
		t.addValues(SampleTimeSeries.createRandomTimeSeries(10, 0, 10, true));
		final MultiTimeSeriesIterator iterator = TimeSeriesUtils.getMultiIterator(Collections.singletonList(t), 3, Long.MIN_VALUE, Long.MAX_VALUE);
		int cnt = 0;
		SampledValueDataPoint prev = null;
		while (iterator.hasNext()) {
			final SampledValueDataPoint sv = iterator.next();
			Assert.assertNotNull(sv);
			final long time = sv.getTimestamp();
			Assert.assertFalse("MultiIterator for a single time series returned an additional point. ", t.isEmpty(time, time));
			int j=0;
			for (j=0;j<Math.min(cnt,3);j++) {
				SampledValueDataPoint previous = sv.getPrevious(j+1);
				Assert.assertNotNull(previous);
				if (j==0)
					Assert.assertEquals("Got wrong previous value",prev, previous);
			}
			if (cnt < 3)
				Assert.assertNull("Got unexpected previous value",sv.getPrevious(cnt+1));
			prev = sv;
			cnt++;
		}
		Assert.assertEquals("MultiIterator for a single time series returned an unexpected number of points. ",t.size(), cnt);
	}
	
	@Test
	public void historicalDataStorageWorksForTwoTimeSeries() {
		final TimeSeries t0 = new TreeTimeSeries(null);
		t0.addValues(SampleTimeSeries.createRandomTimeSeries(3, 0, 10, false));
		final TimeSeries t1 = new TreeTimeSeries(null);
		t1.addValues(SampleTimeSeries.createRandomTimeSeries(3, 10, 10, false));
		final int maxNrHistorical = 2;
		final MultiTimeSeriesIterator iterator = TimeSeriesUtils.getMultiIterator(Arrays.asList(t0, t1), maxNrHistorical, Long.MIN_VALUE, Long.MAX_VALUE);
		SampledValueDataPoint dp = assertPointMatches(iterator, new int[]{0});
		Assert.assertNull(dp.getPrevious(1));
		SampledValueDataPoint prev = dp;
		dp = assertPointMatches(iterator, new int[]{0,1});
		Assert.assertNotNull(dp.getPrevious(1));
		Assert.assertEquals(prev, dp.getPrevious(1));
		Assert.assertNull(dp.getPrevious(2));
		dp = assertPointMatches(iterator, new int[]{0,1});
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void historicalDataSizeLimitIsKept() {
		final TimeSeries t = new TreeTimeSeries(null);
		t.addValues(SampleTimeSeries.createRandomTimeSeries(10, 0, 10, true));
		final int maxNrHistorical = 3;
		final MultiTimeSeriesIterator iterator = TimeSeriesUtils.getMultiIterator(Collections.singletonList(t), maxNrHistorical, Long.MIN_VALUE, Long.MAX_VALUE);
		for (int i=0;i<maxNrHistorical;i++)
			iterator.next();
		final SampledValueDataPoint sv = iterator.next();
	 	sv.getPrevious(maxNrHistorical+1);
	}
	
	@Test(expected=IllegalStateException.class)
	public void historicalDataAccessFromHistoricalDataFails() {
		final TimeSeries t = new TreeTimeSeries(null);
		t.addValues(SampleTimeSeries.createRandomTimeSeries(10, 0, 10, true));
		final int maxNrHistorical = 3;
		final MultiTimeSeriesIterator iterator = TimeSeriesUtils.getMultiIterator(Collections.singletonList(t), maxNrHistorical, Long.MIN_VALUE, Long.MAX_VALUE);
		for (int i=0;i<maxNrHistorical;i++)
			iterator.next();
		final SampledValueDataPoint previous = iterator.next();
		final SampledValueDataPoint current = iterator.next();
		Assert.assertNotNull(current.getPrevious(maxNrHistorical));
		previous.getPrevious(maxNrHistorical);
	}
	
	@Test
	public void addWorksForModeNone() {
		// two time series with following values:
		// (0: 1), (10: 1), (20: 1,2), (30: 1), (40: 1), (45: 2), (50: 1), (60: 1), (70: 1,2), (80: 1), (90: 1), (95: 2), (120: 2)
		final TimeSeries t0 = new TreeTimeSeries(InterpolationMode.NONE);
		t0.addValues(SampleTimeSeries.createStepFunction(10, 0, 10, 10, 0)); // constant function = 10
		final TimeSeries t1 = new TreeTimeSeries(InterpolationMode.NONE);
		t1.addValues(SampleTimeSeries.createStepFunction(5, 20, 25, 20, 0)); // constant function = 20
		final ReadOnlyTimeSeries sum =  TimeSeriesUtils.addEagerly(Arrays.<ReadOnlyTimeSeries> asList(t0, t1), InterpolationMode.NONE, 0, 120);
		//		final ReadOnlyTimeSeries sum =  MultiTimeSeriesUtils.add(Arrays.<ReadOnlyTimeSeries> asList(t0, t1), 0, 120, true, null, false);
		Assert.assertEquals("Time series sum has wrong number of data points", 13, sum.size());
		Assert.assertEquals("Unexpected value in time series sum", 30, sum.getValue(20).getValue().getIntegerValue());
		Assert.assertEquals("Unexpected value in time series sum", 20, sum.getValue(45).getValue().getIntegerValue());
		Assert.assertEquals("Unexpected value in time series sum", 10, sum.getValue(80).getValue().getIntegerValue());
	}
	
	@Test
	public void averageWorksForModeNone() {
		// two time series with following values:
		// (0: 1), (10: 1), (20: 1,2), (30: 1), (40: 1), (45: 2), (50: 1), (60: 1), (70: 1,2), (80: 1), (90: 1), (95: 2), (120: 2)
		final TimeSeries t0 = new TreeTimeSeries(null);
		t0.addValues(SampleTimeSeries.createStepFunction(10, 0, 10, 10, 0)); // constant function = 10
		final TimeSeries t1 = new TreeTimeSeries(null);
		t1.addValues(SampleTimeSeries.createStepFunction(5, 20, 25, 20, 0)); // constant function = 20
		final ReadOnlyTimeSeries avg =  TimeSeriesUtils.averageEagerly(Arrays.<ReadOnlyTimeSeries> asList(t0, t1), InterpolationMode.STEPS, 0, Long.MAX_VALUE);
//		final ReadOnlyTimeSeries avg =  MultiTimeSeriesUtils.getAverageTimeSeries(Arrays.<ReadOnlyTimeSeries> asList(t0, t1), 0, Long.MAX_VALUE, true, null, false);
		Assert.assertEquals("Time series sum has wrong number of data points", 13, avg.size());
		Assert.assertEquals("Unexpected value in time series sum", 15, avg.getValue(20).getValue().getIntegerValue());
		Assert.assertEquals("Unexpected value in time series sum", 20, avg.getValue(45).getValue().getIntegerValue());
		Assert.assertEquals("Unexpected value in time series sum", 10, avg.getValue(80).getValue().getIntegerValue());
	}
	
	@Test
	public void addWorksForModeSteps() {
		// two time series with following values:
		// (0: 1), (10: 1), (20: 1,2), (30: 1), (40: 1), (45: 2), (50: 1), (60: 1), (70: 1,2), (80: 1), (90: 1), (95: 2), (120: 2)
		final TimeSeries t0 = new TreeTimeSeries(null);
		t0.addValues(SampleTimeSeries.createStepFunction(10, 0, 10, 10, 0)); // constant function = 10
		final TimeSeries t1 = new TreeTimeSeries(null);
		t1.addValues(SampleTimeSeries.createStepFunction(5, 20, 25, 20, 0)); // constant function = 20
		t0.setInterpolationMode(InterpolationMode.STEPS);
		t1.setInterpolationMode(InterpolationMode.STEPS);
		final ReadOnlyTimeSeries sum =  TimeSeriesUtils.addEagerly(Arrays.<ReadOnlyTimeSeries> asList(t0, t1), InterpolationMode.STEPS, 5, 92);
//		final ReadOnlyTimeSeries sum =  MultiTimeSeriesUtils.add(Arrays.<ReadOnlyTimeSeries> asList(t0, t1), 5, 92, true, null, false);
		// two boundary points should be added; three points are outside the range
		Assert.assertEquals("Time series sum has wrong number of data points", 12, sum.size());
		Assert.assertEquals("Unexpected value in time series sum", 10, sum.getValue(7).getValue().getIntegerValue());
		Assert.assertEquals("Unexpected value in time series sum", 30, sum.getValue(23).getValue().getIntegerValue());
		Assert.assertEquals("Unexpected value in time series sum", 30, sum.getValue(100).getValue().getIntegerValue());
	}
	
	@Test
	public void averageWorksForModeSteps() {
		// two time series with following values:
		// (0: 1), (10: 1), (20: 1,2), (30: 1), (40: 1), (45: 2), (50: 1), (60: 1), (70: 1,2), (80: 1), (90: 1), (95: 2), (120: 2)
		final TimeSeries t0 = new TreeTimeSeries(null);
		t0.addValues(SampleTimeSeries.createStepFunction(10, 0, 10, 10, 0)); // constant function = 10
		final TimeSeries t1 = new TreeTimeSeries(null);
		t1.addValues(SampleTimeSeries.createStepFunction(5, 20, 25, 20, 0)); // constant function = 20
		t0.setInterpolationMode(InterpolationMode.STEPS);
		t1.setInterpolationMode(InterpolationMode.STEPS);
		final ReadOnlyTimeSeries avg =  TimeSeriesUtils.averageEagerly(Arrays.<ReadOnlyTimeSeries> asList(t0, t1), null, 0, Long.MAX_VALUE);
		
//		final ReadOnlyTimeSeries avg =  MultiTimeSeriesUtils.getAverageTimeSeries(Arrays.<ReadOnlyTimeSeries> asList(t0, t1), 0, Long.MAX_VALUE, true, null, false);
		// one data point outside time range, one boundary value should be added
		Assert.assertEquals("Time series sum has wrong number of data points", 13, avg.size());
		Assert.assertEquals("Unexpected value in time series sum", 10, avg.getValue(7).getValue().getIntegerValue());
		Assert.assertEquals("Unexpected value in time series sum", 15, avg.getValue(23).getValue().getIntegerValue());
		Assert.assertEquals("Unexpected value in time series sum", 15, avg.getValue(100).getValue().getIntegerValue());
	}
	
	@Test
	public void gapsLeadToBadQuality() {
		// two time series with following values:
		// (0: 1), (10: 1), (20: 1,2), (30: 1), (40: 1), (45: 2), (50: 1), (60: 1), (70: 1,2), (80: 1), (90: 1), (95: 2), (120: 2)
		final TimeSeries t0 = new TreeTimeSeries(null);
		t0.addValues(SampleTimeSeries.createStepFunction(10, 0, 10, 10, 0)); // constant function = 10
		final TimeSeries t1 = new TreeTimeSeries(null);
		t1.addValues(SampleTimeSeries.createStepFunction(5, 20, 25, 20, 0)); // constant function = 20
		t1.addValues(Collections.singletonList(new SampledValue(new FloatValue(234F), 60, Quality.BAD)));
		t0.setInterpolationMode(InterpolationMode.STEPS);
		t1.setInterpolationMode(InterpolationMode.STEPS);
		final ReadOnlyTimeSeries avg =  TimeSeriesUtils.averageEagerly(Arrays.<ReadOnlyTimeSeries> asList(t0, t1), InterpolationMode.STEPS, 0, 110);
//		final ReadOnlyTimeSeries avg =  MultiTimeSeriesUtils.getAverageTimeSeries(Arrays.<ReadOnlyTimeSeries> asList(t0, t1), 0L, 110);
		Assert.assertEquals("Unexpected quality",Quality.GOOD, avg.getValue(59).getQuality());
		Assert.assertEquals("Unexpected quality",Quality.BAD, avg.getValue(60).getQuality());
		Assert.assertEquals("Unexpected quality",Quality.BAD, avg.getValue(63).getQuality());
		Assert.assertEquals("Unexpected quality",Quality.GOOD, avg.getValue(70).getQuality());
	}
	
	@Test
	public void missingBoundaryValuesAreFilledWithBadQualityValues() {
		// two time series with following values (13 in total):
		// (10: 1), (20: 1,2), (30: 1), (40: 1), (45: 2), (50: 1), (60: 1), (70: 1,2), (80: 1), (90: 1), (95: 2), (100: 1), (120: 2)
		final TimeSeries t0 = new TreeTimeSeries(null);
		t0.addValues(SampleTimeSeries.createStepFunction(10, 10, 10, 10, 0)); // constant function = 10
		final TimeSeries t1 = new TreeTimeSeries(null);
		t1.addValues(SampleTimeSeries.createStepFunction(5, 20, 25, 20, 0)); // constant function = 20
		t1.addValues(Collections.singletonList(new SampledValue(new FloatValue(234F), 60, Quality.BAD)));
		t0.setInterpolationMode(InterpolationMode.STEPS);
		t1.setInterpolationMode(InterpolationMode.STEPS);
		final ReadOnlyTimeSeries avg =  TimeSeriesUtils.averageEagerly(Arrays.<ReadOnlyTimeSeries> asList(t0, t1), InterpolationMode.STEPS, 0, 130, false, false, true);
//		final ReadOnlyTimeSeries avg =  MultiTimeSeriesUtils.getAverageTimeSeries(Arrays.<ReadOnlyTimeSeries> asList(t0, t1), 0L, 130);
		Assert.assertEquals("Time series sum has wrong number of data points", 14, avg.size());
		Assert.assertEquals(InterpolationMode.STEPS, avg.getInterpolationMode());
		// now we calculate it again, this time demanding boundary markers
		final ReadOnlyTimeSeries avg2 =  TimeSeriesUtils.averageEagerly(Arrays.<ReadOnlyTimeSeries> asList(t0, t1), InterpolationMode.STEPS, 0, 130, true, false, false);
		Assert.assertEquals("Time series sum has wrong number of data points", 14, avg2.size());
		final ReadOnlyTimeSeries avg3 =  TimeSeriesUtils.averageEagerly(Arrays.<ReadOnlyTimeSeries> asList(t0, t1), InterpolationMode.STEPS, 25, 35, true, false, false);
		Assert.assertEquals("Time series sum has wrong number of data points", 3, avg3.size());
		Assert.assertEquals(Quality.GOOD, avg3.getValue(25).getQuality());
		Assert.assertEquals(Quality.GOOD, avg3.getValue(29).getQuality());
		Assert.assertEquals(Quality.BAD, avg2.getValue(10).getQuality());
		Assert.assertEquals(Quality.BAD, avg2.getValue(11).getQuality());
		Assert.assertEquals(Quality.GOOD, avg2.getValue(31).getQuality());
		Assert.assertEquals(Quality.GOOD, avg2.getValue(130).getQuality());
	}
	
	// here we use a step size iterator on time series with no points defined at the requested interval steps
	@Test
	public void stepSizeWorks() {
		final TimeSeries t0 = new TreeTimeSeries(null);
		t0.addValues(SampleTimeSeries.createStepFunction(10, 0, 10, 0, 10)); // linear function y = t
		t0.setInterpolationMode(InterpolationMode.LINEAR);
		final TimeSeries t1 = new TreeTimeSeries(null);
		t1.addValues(SampleTimeSeries.createStepFunction(10, 0, 10, 5, 20)); // linear function y = 2 * t + 5
		t1.setInterpolationMode(InterpolationMode.LINEAR);
		final MultiTimeSeriesIterator multiIt = MultiTimeSeriesIteratorBuilder.newBuilder(Arrays.asList(t0.iterator(),t1.iterator()))
				.setStepSize(-5, 10)
				.setGlobalInterpolationMode(InterpolationMode.LINEAR)
				.build();
		Assert.assertTrue("Multi iterator not correctly initialized", multiIt.hasNext());
		for (int i=0;i<t0.size()-1;i++) {
			Assert.assertTrue("Iterator lacks points", multiIt.hasNext());
			SampledValueDataPoint point = multiIt.next();
			final long t = point.getTimestamp();
			SampledValue first = point.getElement(0, InterpolationMode.LINEAR);
			SampledValue second = point.getElement(1, InterpolationMode.LINEAR);
			Assert.assertEquals("Unexpected time stamp in step size iterator", 5+10*i, t);
			Assert.assertEquals("Step size iterator: unexpected value",5 + 10*i, first.getValue().getFloatValue(), 0.01F);
			Assert.assertEquals("Step size iterator: unexpected value",15 + 20*i, second.getValue().getFloatValue(), 0.01F);
			if (i > 0) {
				first = point.previous(0);
				second = point.previous(1);
				Assert.assertEquals("Iterator's previous failed: unexpected time stamp", 10*i, first.getTimestamp());
				Assert.assertEquals("Step size iterator: unexpected previous value", 10*i, first.getValue().getFloatValue(), 0.01F);
				Assert.assertEquals("Step size iterator: unexpected previous value", 5 + 20*i, second.getValue().getFloatValue(), 0.01F);
			}
			if (i < t0.size()-2) {
				first = point.next(0);
				second = point.next(1);
				Assert.assertEquals("Iterator's next failed: unexpected time stamp", 10*(i+1), first.getTimestamp());
				Assert.assertEquals("Step size iterator: unexpected previous value", 10*(i+1), first.getValue().getFloatValue(), 0.01F);
				Assert.assertEquals("Step size iterator: unexpected previous value", 25 + 20*i, second.getValue().getFloatValue(), 0.01F);
			}
		}
	}
	
	@Test
	public void stepSizeWorksWithMatchingPoints() {
		final TimeSeries t0 = new TreeTimeSeries(null);
		t0.addValues(SampleTimeSeries.createStepFunction(10, 0, 10, 0, 10)); // linear function y = t
		final int sz = t0.size();
		t0.addValue(5, new FloatValue(5));
		t0.addValue(7, new FloatValue(7));
		t0.addValue(13, new FloatValue(13));
		t0.addValue(15, new FloatValue(15));
		t0.addValue(16, new FloatValue(16));
		t0.setInterpolationMode(InterpolationMode.LINEAR);
		final TimeSeries t1 = new TreeTimeSeries(null);
		t1.addValues(SampleTimeSeries.createStepFunction(10, 0, 10, 5, 20)); // linear function y = 2 * t + 5
		t1.addValue(75, new FloatValue(155));
		t1.setInterpolationMode(InterpolationMode.LINEAR);
		final MultiTimeSeriesIterator multiIt = MultiTimeSeriesIteratorBuilder.newBuilder(Arrays.asList(t0.iterator(),t1.iterator()))
				.setStepSize(-5, 10)
				.setGlobalInterpolationMode(InterpolationMode.LINEAR)
				.build();
		Assert.assertTrue("Multi iterator not correctly initialized", multiIt.hasNext());
		for (int i=0;i<sz-1;i++) {
			Assert.assertTrue("Iterator lacks points", multiIt.hasNext());
			SampledValueDataPoint point = multiIt.next();
			final long t = point.getTimestamp();
			SampledValue first = point.getElement(0, InterpolationMode.LINEAR);
			SampledValue second = point.getElement(1, InterpolationMode.LINEAR);
			Assert.assertEquals("Unexpected time stamp in step size iterator", 5+10*i, t);
			Assert.assertEquals("Step size iterator: unexpected value",5 + 10*i, first.getValue().getFloatValue(), 0.01F);
			Assert.assertEquals("Step size iterator: unexpected value",15 + 20*i, second.getValue().getFloatValue(), 0.01F);
		}
	}
	
	// here we use a step size iterator on time series with no points defined at the requested interval steps
	@Test
	public void stepSizeWorksWithHistory() {
		final TimeSeries t0 = new TreeTimeSeries(null);
		t0.addValues(SampleTimeSeries.createStepFunction(10, 0, 10, 0, 10)); // linear function y = t
		t0.setInterpolationMode(InterpolationMode.LINEAR);
		final TimeSeries t1 = new TreeTimeSeries(null);
		t1.addValues(SampleTimeSeries.createStepFunction(10, 0, 10, 5, 20)); // linear function y = 2 * t + 5
		t1.setInterpolationMode(InterpolationMode.LINEAR);
		final MultiTimeSeriesIterator multiIt = MultiTimeSeriesIteratorBuilder.newBuilder(Arrays.asList(t0.iterator(),t1.iterator()))
				.setStepSize(-5, 10)
				.setGlobalInterpolationMode(InterpolationMode.LINEAR)
				.setMaxNrHistoricalValues(1)
				.build();
		Assert.assertTrue("Multi iterator not correctly initialized", multiIt.hasNext());
		for (int i=0;i<t0.size()-1;i++) {
			Assert.assertTrue("Iterator lacks points", multiIt.hasNext());
			SampledValueDataPoint point = multiIt.next();
			if (i > 0) {
				SampledValueDataPoint previous = point.getPrevious(1);
				Assert.assertEquals("Iterator's getPrevious failed: unexpected time stamp", 5+10*(i-1), previous.getTimestamp());
				Assert.assertEquals("Iterator's getPreviousTimestamp failed: unexpected time stamp", 5+10*(i-1), point.getPreviousTimestamp());
				SampledValue first = previous.getElement(0, InterpolationMode.LINEAR);
				SampledValue second = previous.getElement(1, InterpolationMode.LINEAR);
				Assert.assertEquals("Step size iterator: unexpected previous value",5 + 10*(i-1), first.getValue().getFloatValue(), 0.01F);
				Assert.assertEquals("Step size iterator: unexpected previous value",15 + 20*(i-1), second.getValue().getFloatValue(), 0.01F);
			}
		}
	}
	
	@Test
	public void stepRulersWork() {
		final TimeSeries t0 = new TreeTimeSeries(null);
		t0.addValues(SampleTimeSeries.createStepFunction(10, 0, 10, 0, 10)); // linear function y = t
		t0.setInterpolationMode(InterpolationMode.LINEAR);
		final TimeSeries t1 = new TreeTimeSeries(null);
		t1.addValues(SampleTimeSeries.createStepFunction(10, 5, 10, 5, 20)); // linear function y = 2 * t + 10
		t1.setInterpolationMode(InterpolationMode.LINEAR);
		final MultiTimeSeriesIterator multiIt = MultiTimeSeriesIteratorBuilder.newBuilder(Arrays.asList(t0.iterator(),t1.iterator()))
				.stepSizeAsInSchedules(new int[]{0})
				.setGlobalInterpolationMode(InterpolationMode.LINEAR)
				.setMaxNrHistoricalValues(1)
				.build();
		for (int i=0;i<t0.size();i++) {
			Assert.assertTrue(multiIt.hasNext());
			SampledValueDataPoint point = multiIt.next();
			Assert.assertEquals("Unexpected timestamp",t0.getValues(Long.MIN_VALUE).get(i).getTimestamp(), point.getTimestamp());
		}
	}
	
	@Test
	public void integrationWorksWithOrdinaryIterator() {
		// integral over t should be equal to = 4500
		final TimeSeries t = new TreeTimeSeries(null);
		t.addValues(SampleTimeSeries.createStepFunction(10, 0, 10, 0, 10)); // step function from 0 to 100
		t.setInterpolationMode(InterpolationMode.STEPS);
		final TimeSeries tr = new TreeTimeSeries(null);
		tr.addValues(SampleTimeSeries.createStepFunction(5, 0, 20, 0, 0)); // step function with doubled step size
		tr.setInterpolationMode(InterpolationMode.STEPS);
		final MultiTimeSeriesIterator multiIt = MultiTimeSeriesIteratorBuilder.newBuilder(Arrays.asList(t.iterator(),tr.iterator()))
				.setGlobalInterpolationMode(InterpolationMode.STEPS)
				.doIntegrate(true)
				.setUpperBoundaryValues(Collections.singletonMap(0, new SampledValue(new FloatValue(100), 100, Quality.GOOD)))
				.build();
		SampledValueDataPoint sv = null;
		SampledValue value = null;
		Map<Integer,SampledValue> map = null;
		while (multiIt.hasNext()) {
			sv = multiIt.next();
			map = sv.getElements();
			if (map.containsKey(0)) 
				value = map.get(0);
		}
		Assert.assertNotNull(value);
		Assert.assertEquals("Integration failed", 4500, value.getValue().getFloatValue(), 5);
	}
	
	@Test
	public void integrationWorksWithStepRulers() {
		// integral over t should be independent of the step ruler = 4500
		final TimeSeries t = new TreeTimeSeries(null);
		t.addValues(SampleTimeSeries.createStepFunction(10, 0, 10, 0, 10)); // step function from 0 to 100
		t.setInterpolationMode(InterpolationMode.STEPS);
		final TimeSeries tr = new TreeTimeSeries(null);
		tr.addValues(SampleTimeSeries.createStepFunction(5, 0, 20, 0, 0)); // step function with doubled step size
		tr.setInterpolationMode(InterpolationMode.STEPS);
		final Map<Integer,SampledValue> upperBoundaries = new HashMap<>();
		upperBoundaries.put(0, new SampledValue(new FloatValue(100), 100, Quality.GOOD));
		upperBoundaries.put(1, new SampledValue(new FloatValue(0), 100, Quality.GOOD));
		final MultiTimeSeriesIterator multiIt = MultiTimeSeriesIteratorBuilder.newBuilder(Arrays.asList(t.iterator(),tr.iterator()))
				.stepSizeAsInSchedules(new int[]{1})
				.setGlobalInterpolationMode(InterpolationMode.STEPS)
				.setUpperBoundaryValues(upperBoundaries)
				.doIntegrate(true)
				.build();
		SampledValueDataPoint sv = null;
		while (multiIt.hasNext())
			sv = multiIt.next();
		Assert.assertNotNull(sv);
		Assert.assertNotNull(sv.getElements());
		final SampledValue value0 = sv.getElements().get(0);
		Assert.assertNotNull(value0);
		Assert.assertEquals("Integration failed", 4500, value0.getValue().getFloatValue(), 5);
		final SampledValue value1 = sv.getElements().get(1);
		Assert.assertNotNull(value1);
		Assert.assertEquals("Integration failed", 0, value1.getValue().getFloatValue(), 1);

	}
	
	@Test
	public void integrationWorksWithFixedStepSize1() {
		final TimeSeries t0 = new TreeTimeSeries(null);
		t0.addValues(SampleTimeSeries.createStepFunction(10, 0, 10, 0, 10)); // linear function y = t
		t0.setInterpolationMode(InterpolationMode.LINEAR);
		final TimeSeries t1 = new TreeTimeSeries(null);
		t1.addValues(SampleTimeSeries.createStepFunction(10, 5, 10, 5, 0)); // constant function y = 5
		t1.setInterpolationMode(InterpolationMode.STEPS);
		
		final MultiTimeSeriesIterator multiIt = MultiTimeSeriesIteratorBuilder.newBuilder(Arrays.asList(t0.iterator(),t1.iterator()))
				.setStepSize(0, 10)
				.setIndividualInterpolationModes(Arrays.asList(InterpolationMode.LINEAR, InterpolationMode.STEPS))
				.doIntegrate(true)
				.build();
		for (int i=0;i<t0.size()-1;i++) {
			Assert.assertTrue("Iterator lacks points", multiIt.hasNext());
			SampledValueDataPoint point = multiIt.next();
			if (i > 0) {
				SampledValue sv = point.getElement(0);
				Assert.assertEquals("Integration failed; unexpected value",100/2*(i*i-(i-1)*(i-1)), sv.getValue().getFloatValue(), 0.5F);
				sv = point.getElement(1);
				if (i > 1)
					Assert.assertEquals("Integration failed; unexpected value",50, sv.getValue().getFloatValue(), 0.5F);
			}
		}
	}
	
	@Test
	public void averagingWorksWithOrdinaryIterator() {
		// average over t should be equal to = 45
		final TimeSeries t = new TreeTimeSeries(null);
		t.addValues(SampleTimeSeries.createStepFunction(10, 0, 10, 0, 10)); // step function from 0 to 100
		t.setInterpolationMode(InterpolationMode.STEPS);
//		final TimeSeries tr = new TreeTimeSeries(null);
//		tr.addValues(SampleTimeSeries.createStepFunction(5, 0, 20, 0, 0)); // step function with doubled step size
//		tr.setInterpolationMode(InterpolationMode.STEPS);
//		final MultiTimeSeriesIterator multiIt = MultiTimeSeriesIteratorBuilder.newBuilder(Arrays.asList(t.iterator(),tr.iterator()))
		final MultiTimeSeriesIterator multiIt = MultiTimeSeriesIteratorBuilder.newBuilder(Arrays.asList(t.iterator()))
				.setGlobalInterpolationMode(InterpolationMode.STEPS)
				.doAverage(true)
				.setUpperBoundaryValues(Collections.singletonMap(0, new SampledValue(new FloatValue(100), 100, Quality.GOOD)))
				.build();
		SampledValueDataPoint sv = null;
		SampledValue value = null;
		Map<Integer,SampledValue> map = null;
		int cnt = 0;
		Assert.assertTrue(multiIt.hasNext());
		multiIt.next();
		while (multiIt.hasNext()) {
			sv = multiIt.next();
			map = sv.getElements();
			value = map.get(0);
			Assert.assertEquals("Averaging failed", cnt++ * 10, value.getValue().getFloatValue(), 1);
			if (map.containsKey(1))
				Assert.assertEquals("Averaging failed", 0, map.get(1).getValue().getFloatValue(), 1);
		}
		Assert.assertTrue(cnt > 3);
	}
	
	@Test
	public void averagingWorksWithFixedStepSize1() {
		// average over t should be equal to = 45
		final TimeSeries t = new TreeTimeSeries(null);
		t.addValues(SampleTimeSeries.createStepFunction(10, 0, 10, 0, 10)); // step function from 0 to 100
		t.setInterpolationMode(InterpolationMode.STEPS);
		final TimeSeries tr = new TreeTimeSeries(null);
		tr.addValues(SampleTimeSeries.createStepFunction(5, 0, 20, 0, 0)); // step function with doubled step size
		tr.setInterpolationMode(InterpolationMode.STEPS);
		final MultiTimeSeriesIterator multiIt = MultiTimeSeriesIteratorBuilder.newBuilder(Arrays.asList(t.iterator(),tr.iterator()))
				.setGlobalInterpolationMode(InterpolationMode.STEPS)
				.setStepSize(0, 20)
				.doAverage(true)
				.setUpperBoundaryValues(Collections.singletonMap(0, new SampledValue(new FloatValue(100), 100, Quality.GOOD)))
				.build();
		SampledValueDataPoint sv = null;
		SampledValue value = null;
		Map<Integer,SampledValue> map = null;
		int cnt = 0;
		Assert.assertTrue(multiIt.hasNext());
		multiIt.next();
		while (multiIt.hasNext()) {
			sv = multiIt.next();
			map = sv.getElements();
			value = map.get(0);
			Assert.assertEquals("Averaging failed", cnt++ * 20 + 5, value.getValue().getFloatValue(), 1);
			Assert.assertEquals("Averaging failed", 0, sv.getElements().get(1).getValue().getFloatValue(), 1);
		}
		Assert.assertTrue(cnt > 3);
	}

	@Test
	public void averagingWorksWithFixedStepSize2() {
		final TimeSeries t = new TreeTimeSeries(null);
		t.addValue(5, new FloatValue(10));
		t.addValue(15, new FloatValue(20));
		t.addValue(40, new FloatValue(20));
		final MultiTimeSeriesIterator it = MultiTimeSeriesIteratorBuilder.newBuilder(Arrays.asList(t.iterator()))
				.setGlobalInterpolationMode(InterpolationMode.LINEAR)
				.setStepSize(0, 10)
				.doAverage(true)
				.build();
		Assert.assertTrue(it.hasNext());
		final SampledValueDataPoint first = it.next();
		Assert.assertEquals(10, first.getTimestamp());
		Assert.assertEquals(12.5, first.getElements().get(0).getValue().getFloatValue(), 0.1F);
		Assert.assertTrue(it.hasNext());
		final SampledValueDataPoint second = it.next();
		Assert.assertEquals(20, second.getTimestamp());
		Assert.assertEquals((17.5 + 20)/2, second.getElements().get(0).getValue().getFloatValue(), 0.1F);
		final SampledValueDataPoint third = it.next();
		Assert.assertEquals(30, third.getTimestamp());
		Assert.assertEquals(20, third.getElements().get(0).getValue().getFloatValue(), 0.1F);
		final SampledValueDataPoint fourth = it.next();
		Assert.assertEquals(40, fourth.getTimestamp());
		Assert.assertEquals(20, fourth.getElements().get(0).getValue().getFloatValue(), 0.1F);
		Assert.assertFalse(it.hasNext());
	}
	
	
	@Test
	public void averagingWorksWithStepRulers() {
		// average over t should be equal to = 45
		final TimeSeries t = new TreeTimeSeries(null);
		t.addValues(SampleTimeSeries.createStepFunction(10, 0, 10, 0, 10)); // step function from 0 to 100
		t.setInterpolationMode(InterpolationMode.STEPS);
		final TimeSeries tr = new TreeTimeSeries(null);
		tr.addValues(SampleTimeSeries.createStepFunction(5, 0, 20, 0, 0)); // step function with doubled step size
		tr.setInterpolationMode(InterpolationMode.STEPS);
		final MultiTimeSeriesIterator multiIt = MultiTimeSeriesIteratorBuilder.newBuilder(Arrays.asList(t.iterator(),tr.iterator()))
				.setGlobalInterpolationMode(InterpolationMode.STEPS)
				.stepSizeAsInSchedules(new int[]{1})
				.doAverage(true)
				.setUpperBoundaryValues(Collections.singletonMap(0, new SampledValue(new FloatValue(100), 100, Quality.GOOD)))
				.build();
		SampledValueDataPoint sv = null;
		SampledValue value = null;
		Map<Integer,SampledValue> map = null;
		int cnt = 0;
		Assert.assertTrue(multiIt.hasNext());
		multiIt.next();
		while (multiIt.hasNext()) {
			sv = multiIt.next();
			map = sv.getElements();
			value = map.get(0);
			Assert.assertEquals("Averaging failed", cnt++ * 20 + 5, value.getValue().getFloatValue(), 1);
			Assert.assertEquals("Averaging failed", 0, map.get(1).getValue().getFloatValue(), 1);
		}
		Assert.assertTrue(cnt > 3);
	}
	
	@Test
	public void diffIterationWorks() {
		final TimeSeries t = new TreeTimeSeries(null);
		t.addValues(SampleTimeSeries.createStepFunction(3, 0, 10, 0, 10)); // step function from 0 to 20
		t.setInterpolationMode(InterpolationMode.LINEAR);
		final MultiTimeSeriesIterator it = MultiTimeSeriesIteratorBuilder.newBuilder(Collections.singletonList(t.iterator()))
			.doDiff(true)
			.build();
		final float expected = 10;
		long t0 = -5;
		int cnt = 0;
		while (it.hasNext()) {
			t0 = t0 + 10;
			cnt++;
			final SampledValueDataPoint point = it.next();
			final SampledValue sv = point.getElements().get(0);
			final long time = sv.getTimestamp();
			final float v = sv.getValue().getFloatValue();
			Assert.assertEquals(expected, v, 0.1F);
			Assert.assertEquals(t0, time);
		}
		Assert.assertEquals(2, cnt);
	}
	
	@Test
	public void diffIterationStepSizeWorks() {
		final TimeSeries t = new TreeTimeSeries(null);
		t.addValue(0, new FloatValue(10));
		t.addValue(10, FloatValue.ZERO);
		t.addValue(15, new FloatValue(10));
		t.addValue(30, new FloatValue(40));
		t.setInterpolationMode(InterpolationMode.LINEAR);
		final MultiTimeSeriesIterator it = MultiTimeSeriesIteratorBuilder.newBuilder(Collections.singletonList(t.iterator()))
			.doDiff(true, 1)
			.setStepSize(0, 10)
			.setGlobalInterpolationMode(InterpolationMode.LINEAR)
			.build();
		Assert.assertTrue(it.hasNext());
		final SampledValueDataPoint first = it.next();
		final SampledValue firstSv = first.getElements().get(0);
		Assert.assertEquals(-10, firstSv.getValue().getFloatValue(), 0.1);
		Assert.assertEquals(10, first.getTimestamp());
		
		Assert.assertTrue(it.hasNext());
		final SampledValueDataPoint second = it.next();
		final SampledValue secondSv = second.getElements().get(0);
		Assert.assertEquals(20, secondSv.getValue().getFloatValue(), 0.1);
		Assert.assertEquals(20, secondSv.getTimestamp());
		
		Assert.assertTrue(it.hasNext());
		final SampledValueDataPoint third = it.next();
		final SampledValue thirdSv = third.getElements().get(0);
		Assert.assertEquals(20, thirdSv.getValue().getFloatValue(), 0.1);
		Assert.assertEquals(30, thirdSv.getTimestamp());
		
		Assert.assertFalse(it.hasNext());
	}
	
	@Test
	public void diffIterationStepSizeWorks2() {
		final TimeSeries t = new TreeTimeSeries(null);
		t.addValue(-10, new FloatValue(10));
		t.addValue(10, FloatValue.ZERO);
		t.addValue(20, new FloatValue(10));
		t.addValue(30, new FloatValue(40));
		t.addValue(40, new FloatValue(40));
		t.addValue(60, new FloatValue(60));
		t.addValue(210, new FloatValue(210));
		t.setInterpolationMode(InterpolationMode.LINEAR);
		final MultiTimeSeriesIterator it = MultiTimeSeriesIteratorBuilder.newBuilder(Collections.singletonList(t.iterator()))
			.doDiff(true, 0)
			.setStepSize(0, 50)
			.setGlobalInterpolationMode(InterpolationMode.LINEAR)
			.build();
		Assert.assertTrue(it.hasNext());
		final SampledValueDataPoint first = it.next();
		final SampledValue firstSv = first.getElements().get(0);
		Assert.assertEquals(45, firstSv.getValue().getFloatValue(), 0.1);
		Assert.assertEquals(0, first.getTimestamp());
		
		Assert.assertTrue(it.hasNext());
		final SampledValueDataPoint second = it.next();
		final SampledValue secondSv = second.getElements().get(0);
		Assert.assertEquals(50, secondSv.getValue().getFloatValue(), 0.1);
		Assert.assertEquals(50, second.getTimestamp());
		
		Assert.assertTrue(it.hasNext());
		final SampledValueDataPoint third = it.next();
		final SampledValue thirdSv = third.getElements().get(0);
		Assert.assertEquals(50, thirdSv.getValue().getFloatValue(), 0.1);
		Assert.assertEquals(100, third.getTimestamp());
	}
	
	private static SampledValueDataPoint assertPointMatches(MultiTimeSeriesIterator iterator, int[] expectedIndices) {
		Assert.assertTrue("MultiTimeSeriesIterator lacks point.",iterator.hasNext());
		final SampledValueDataPoint sv = iterator.next();
		final Map<Integer,SampledValue> elements = sv.getElements();
		Assert.assertEquals("Unexpected number of points included in DataPoint.",expectedIndices.length, elements.size());
		for (int n : expectedIndices) {
			Assert.assertTrue("Expected time series index missing in data point.", elements.keySet().contains(n));
		}
		return sv;
	}
	
}
