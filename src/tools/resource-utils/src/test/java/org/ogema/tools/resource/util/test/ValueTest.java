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
package org.ogema.tools.resource.util.test;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ogema.core.channelmanager.measurements.DoubleValue;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.core.timeseries.ReadOnlyTimeSeries;
import org.ogema.exam.OsgiAppTestBase;
import org.ogema.tools.resource.util.ValueResourceUtils;
import org.ogema.tools.timeseries.api.FloatTimeSeries;
import org.ogema.tools.timeseries.implementations.FloatTreeTimeSeries;
import org.ogema.tools.timeseries.iterator.api.MultiTimeSeriesIterator;
import org.ogema.tools.timeseries.iterator.api.MultiTimeSeriesIteratorBuilder;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.junit.Assert;

@ExamReactorStrategy(PerClass.class)
public class ValueTest extends OsgiAppTestBase {

	private ResourceManagement rm;
//	private ResourceAccess ra;
	private StringResource stringRes;
	private FloatResource floatRes;
	private BooleanResource booleanRes;
	private IntegerResource intRes;
	private TimeResource timeRes;

	public ValueTest() {
		super(true);
	}

	@Before
	public void setup() {
		rm = getApplicationManager().getResourceManagement();
//		ra = getApplicationManager().getResourceAccess();
		stringRes = rm.createResource("stringRes", StringResource.class);
		floatRes = rm.createResource("floatRes", FloatResource.class);
		booleanRes = rm.createResource("booleanRes", BooleanResource.class);
		intRes = rm.createResource("intRes", IntegerResource.class);
		timeRes = rm.createResource("timeRes", TimeResource.class);
	}

	@After
	public void cleanUp() {
		stringRes.delete();
		floatRes.delete();
		booleanRes.delete();
		intRes.delete();
		timeRes.delete();
	}

	@Test
	public void setAndGetValueTest() {
		String str = "23";
		ValueResourceUtils.setValue(intRes, str);
		String msg = "Unexpected value";
		assertEquals(msg, 23, intRes.getValue());
		assertEquals(str, ValueResourceUtils.getValue(intRes));
		str = "23.34";
		ValueResourceUtils.setValue(floatRes, str);
		assertEquals(msg, 23.34, floatRes.getValue(), 0.01);
		assertEquals(str, ValueResourceUtils.getValue(floatRes));
		str = "32142324331";
		ValueResourceUtils.setValue(timeRes, str);
		assertEquals(Long.parseLong(str), timeRes.getValue());
		assertEquals(str, ValueResourceUtils.getValue(timeRes));
	}
	
	@Test
	public void integrate2Works1() {
		final FloatTimeSeries t = new FloatTreeTimeSeries();
		t.addValue(0, FloatValue.ZERO);
		t.addValue(10, new FloatValue(10));
		t.addValue(11, FloatValue.ZERO);
		t.addValue(21, new FloatValue(10));
		t.setInterpolationMode(InterpolationMode.LINEAR);
		final double d = ValueResourceUtils.integrate2(t);
		Assert.assertEquals("Unexpected integration result", 105, d, 1);
	}
	
	@Test
	public void integrate2Works2() {
		final FloatTimeSeries t = new FloatTreeTimeSeries();
		t.addValue(0, new FloatValue(10));
		t.addValue(10, FloatValue.ZERO);
		t.addValue(15, new FloatValue(10));
		t.addValue(20, FloatValue.ZERO);
		t.setInterpolationMode(InterpolationMode.STEPS);
		final double d = ValueResourceUtils.integrate2(t);
		Assert.assertEquals("Unexpected integration result", 150, d, 1);
	}
	
	@Test
	public void integration2Works3() {
		final FloatTimeSeries t = new FloatTreeTimeSeries();
		final double value = 10;
		final Value v = new DoubleValue(value);
		t.addValue(0, v);
		t.addValue(15, v);
		t.addValue(30, v);
		t.addValue(45, v);
		t.addValue(60, v);
		t.setInterpolationMode(InterpolationMode.LINEAR);
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
		final FloatTimeSeries t = new FloatTreeTimeSeries();
		final double value = 10;
		final Value v = new DoubleValue(value);
		t.addValue(0, v);
		t.addValue(15, v);
		t.addValue(30, v);
		t.addValue(45, v);
		t.setInterpolationMode(mode);
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
		Assert.assertEquals("Unexpected average value of timeseries " + ts + " over interval " + start + " - " + end, expected, ValueResourceUtils.getAverage(ts, start, end), 0.001);
	}
	
	private static double getAverage(final ReadOnlyTimeSeries ts, final long start, final long end) {
		final double integral = ValueResourceUtils.integrate2(ts, start, end);
		return integral / (end - start);
	}

}
