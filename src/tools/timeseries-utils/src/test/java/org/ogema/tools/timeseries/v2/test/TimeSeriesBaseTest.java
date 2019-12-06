package org.ogema.tools.timeseries.v2.test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.core.timeseries.ReadOnlyTimeSeries;
import org.ogema.tools.timeseries.v2.base.ReadOnlyTimeSeriesBase;
import org.ogema.tools.timeseries.v2.tools.SampleTimeSeries;

public class TimeSeriesBaseTest {
	
	@Test
	public void getNextWorks() {
		final long startTime = 70;
		final long interval = 20;
		final int nrPoints = 10;
		final long offset = 3;
		final IntStream exactTestPoints = IntStream.builder().add(0).add(3).add(7).add(9).build();
		final IntStream intermediateTestPoints = IntStream.builder().add(0).add(3).add(7).build();
		final List<SampledValue> values = SampleTimeSeries.createRandomTimeSeries(nrPoints, startTime, interval, false);
		final ReadOnlyTimeSeries timeSeries = new TsImpl(values);
		exactTestPoints.forEach(i -> testForExistingPoint(timeSeries, startTime + i * interval));
		intermediateTestPoints.forEach(i -> testForIntermediatePoint(timeSeries, startTime + offset + i * interval, 
				offset, interval - offset));
	}

	private static void testForIntermediatePoint(final ReadOnlyTimeSeries timeSeries, final long t, 
			final long expectedDiffPrevious, final long expectedDiffNext) {
		final SampledValue next = timeSeries.getNextValue(t);
		Assert.assertNotNull("Data point missing", next);
		Assert.assertEquals("Unexpexted next timestamp", t + expectedDiffNext, next.getTimestamp());
		final SampledValue expectedNext = timeSeries.getValue(t + expectedDiffNext);
		Assert.assertNotNull("Data point missing", expectedNext);
		Assert.assertEquals("Unexpected next data point", expectedNext, next); 
		final SampledValue previous = timeSeries.getPreviousValue(t);
		Assert.assertNotNull("Data point missing", previous);
		Assert.assertEquals("Unexpexted previous timestamp", t - expectedDiffPrevious, previous.getTimestamp());
		final SampledValue expectedPrevious = timeSeries.getValue(t - expectedDiffPrevious);
		Assert.assertEquals("Unexpected previous data point", expectedPrevious, previous);
	}
	
	private static void testForExistingPoint(final ReadOnlyTimeSeries timeSeries, final long t) {
		final SampledValue next = timeSeries.getNextValue(t);
		Assert.assertNotNull("Data point missing", next);
		Assert.assertEquals("Unexpexted next timestamp", t, next.getTimestamp());
		final SampledValue current = timeSeries.getValue(t);
		Assert.assertNotNull("Data point missing", current);
		Assert.assertEquals("Current and next data point not equal", current, next); 
		final SampledValue previous = timeSeries.getPreviousValue(t);
		Assert.assertNotNull("Data point missing", previous);
		Assert.assertEquals("Unexpexted previous timestamp", t, previous.getTimestamp());
		Assert.assertEquals("Current and previous data point not equal", current, previous); 
	}
	
	
	private static final class TsImpl implements ReadOnlyTimeSeriesBase {
		
		private final List<SampledValue> values;
		
		public TsImpl(final List<SampledValue> values) {
			this.values = values;
		}
		
		@Override
		public List<SampledValue> getValues(long startTime, long endTime) {
			return values.stream()
				.filter(sv -> sv.getTimestamp() >= startTime && sv.getTimestamp() < endTime)
				.collect(Collectors.toList());
		}

		@Override
		public InterpolationMode getInterpolationMode() {
			return InterpolationMode.NONE;
		}
		
	}

}
