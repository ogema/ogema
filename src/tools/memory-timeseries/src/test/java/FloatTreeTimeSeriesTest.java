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
/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * OGEMA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * OGEMA. If not, see <http://www.gnu.org/licenses/>.
 */
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.ogema.tools.timeseries.api.MemoryTimeSeries;
import org.ogema.tools.timeseries.implementations.FloatTreeTimeSeries;
import subtests.FloatTimeSeriesTests;
import static org.junit.Assert.*;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.tools.timeseries.api.FloatTimeSeries;
import org.ogema.tools.timeseries.api.TimeInterval;

/**
 * Tests for the implementation FloatTreeTimeSeries, that implements the
 * FloatTimeSeries interface which in turn defines an algebra on float-valued
 * time series.
 *
 * TODO add tests for the algebra
 *
 * @author Timo Fischer, Fraunhofer IWES
 */
public class FloatTreeTimeSeriesTest {

	/**
	 * Creates an equidistant FloatTimeSeries over [t0; t0+dt; ... t1) with
	 * values equal to x = a*t+b and set its interpolation mode.
	 */
	FloatTreeTimeSeries createSeries(long t0, long t1, long dt, float a, float b, InterpolationMode mode) {
		assert dt > 0;

		final FloatTreeTimeSeries result = new FloatTreeTimeSeries();
		assert result.getValues(0).isEmpty();

		for (long t = t0; t < t1; t += dt) {
			final float x = a * t + b;
			final SampledValue value = new SampledValue(new FloatValue(x), t, Quality.GOOD);
			result.addValue(value);
		}
		final long N = ((t1 - t0) % dt == 0) ? (t1 - t0) / dt : (t1 - t0) / dt + 1;
		assert result.getValues(0).size() == N;
		assert result.getValues(t0, t1).size() == N;

		result.setInterpolationMode(mode);
		assert result.getInterpolationMode() == mode;
		return result;
	}

	/**
	 * Tests the basic methods of a time series that are not associated to the
	 * algebra.
	 */
	@Test
	public void testBasicTimeSeriesMethods() {
		MemoryTimeSeries timeSeries = new FloatTreeTimeSeries();
		FloatTimeSeriesTests tests = new FloatTimeSeriesTests(timeSeries);
		tests.performAllTests();
	}

	@Test
	public void testAddition() {
		final FloatTreeTimeSeries f1 = createSeries(0, 600, 40, 0.f, 1.f, InterpolationMode.LINEAR);
		final FloatTreeTimeSeries f2 = createSeries(0, 1200, 30, 0.f, -1.f, InterpolationMode.LINEAR);
		final FloatTreeTimeSeries sum = new FloatTreeTimeSeries(f2);
		sum.add(f1);

		final SampledValue v500 = sum.getValue(500);
		final SampledValue v1000 = sum.getValue(1000);

		assert v500.getQuality() == Quality.GOOD; // both schedules define value here
		assert v1000.getQuality() == Quality.BAD; // f1 had no defined value, making sum undefined.

		assert v500.getValue().getFloatValue() == 0.f;
	}

	/**
	 * Manually integrate the functio a*x+b over [t0;t1].
	 */
	private float integrate(long t0, long t1, float a, float b) {
		final double dt = (float) (t1 - t0);
		return (float) (0.5 * (a * t1 * t1 - a * t0 * t0) + b * dt);
	}

	@Test
	public void testIntegration() {
		final float a = 1.f;
		final float b = 0.f;

		final FloatTreeTimeSeries f = createSeries(0, 1001, 100, a, b, InterpolationMode.LINEAR);
		for (int i = 0; i < 5; ++i) {
			final long t0 = (long) (1000. * Math.random());
			final long t1 = (long) (1000. * Math.random());
			final float integral = f.integrate(t0, t1);
			assertEquals(integral, integrate(t0, t1, a, b), 0.01f);
		}
	}

	@Test
	public void testGetMax() {
		final float a = 1.f;
		final float b = 0.f;

		final FloatTreeTimeSeries f = createSeries(0, 1001, 100, a, b, InterpolationMode.LINEAR);

		final SampledValue max1 = f.getMax(0, 1000);
		final float x1 = max1.getValue().getFloatValue();
		final long t1 = max1.getTimestamp();
		final Quality q1 = max1.getQuality();
		assertEquals(x1, 999.f, 0.1f);
		assert t1 == 999;
		assert q1 == Quality.GOOD;

		f.setInterpolationMode(InterpolationMode.STEPS);
		final SampledValue max2 = f.getMax(0, 1000);
		final float x2 = max2.getValue().getFloatValue();
		final long t2 = max2.getTimestamp();
		final Quality q2 = max2.getQuality();
		assertEquals(x2, 900.f, 0.1f);
		assert t2 == 900;
		assert q2 == Quality.GOOD;

		f.setInterpolationMode(InterpolationMode.LINEAR);
		final SampledValue max3 = f.getMax(2000, 6000);
		assert max3.getQuality() == Quality.BAD;
	}

	@Test
	public void testConstantConstruction() {
		for (int i = 0; i < 5; ++i) {
			final float value = 600.f * (float) Math.random() - 300.f;
			// constructor that is acutally tested.
			final FloatTimeSeries f = new FloatTreeTimeSeries(value);
			for (int j = 0; j < 20; ++j) {
				final long t = Math.round(Math.random() * 1000000. - 500000.);
				final float x = f.getValue(t).getValue().getFloatValue();
				assert x == value;
			}
		}
	}

	@Test
    public void testPositiveIntervalFindingLinear() {
        final List<SampledValue> values = new ArrayList<>();
        values.add(new SampledValue(new FloatValue(0.f), 0, Quality.GOOD));
        values.add(new SampledValue(new FloatValue(1.f), 100, Quality.GOOD));
        values.add(new SampledValue(new FloatValue(1.f), 200, Quality.GOOD));
        values.add(new SampledValue(new FloatValue(1.f), 300, Quality.BAD));
        values.add(new SampledValue(new FloatValue(1.f), 400, Quality.GOOD));
        // no explicit mentioning of zero-crossing at t=500
        values.add(new SampledValue(new FloatValue(-1.f), 600, Quality.GOOD));
        values.add(new SampledValue(new FloatValue(0.f), 700, Quality.GOOD));
        values.add(new SampledValue(new FloatValue(1.f), 800, Quality.GOOD));
        FloatTimeSeries f = new FloatTreeTimeSeries();
        f.addValues(values);
        f.setInterpolationMode(InterpolationMode.LINEAR);
        final List<TimeInterval> positiveIntervals = f.getPositiveDomain(new TimeInterval(Long.MIN_VALUE, Long.MAX_VALUE));
        for (TimeInterval interval : positiveIntervals) {
            System.out.println("[" + interval.getStart() + "; " + interval.getEnd() + ")");
        }
        assertEquals(3, positiveIntervals.size());
        final TimeInterval pos1 = new TimeInterval(0, 200);
        final TimeInterval pos2 = new TimeInterval(400, 500);
        final TimeInterval pos3 = new TimeInterval(700, 800);
        assertEquals(pos1, positiveIntervals.get(0));
        assertEquals(pos2, positiveIntervals.get(1));
        assertEquals(pos3, positiveIntervals.get(2));
    }

	@Test
    public void testPositiveIntervalFindingSteps() {
        final List<SampledValue> values = new ArrayList<>();
        values.add(new SampledValue(new FloatValue(0.f), 0, Quality.GOOD));
        values.add(new SampledValue(new FloatValue(1.f), 100, Quality.GOOD));
        values.add(new SampledValue(new FloatValue(1.f), 200, Quality.GOOD));
        values.add(new SampledValue(new FloatValue(1.f), 300, Quality.BAD));
        values.add(new SampledValue(new FloatValue(1.f), 400, Quality.GOOD));
        // no explicit mentioning of zero-crossing at t=500
        values.add(new SampledValue(new FloatValue(-1.f), 600, Quality.GOOD));
        values.add(new SampledValue(new FloatValue(0.f), 700, Quality.GOOD));
        values.add(new SampledValue(new FloatValue(1.f), 800, Quality.GOOD));
        FloatTimeSeries f = new FloatTreeTimeSeries();
        f.addValues(values);
        f.setInterpolationMode(InterpolationMode.STEPS);
        final List<TimeInterval> positiveIntervals = f.getPositiveDomain(new TimeInterval(Long.MIN_VALUE, Long.MAX_VALUE));
        for (TimeInterval interval : positiveIntervals) {
            System.out.println("[" + interval.getStart() + "; " + interval.getEnd() + ")");
        }
        assertEquals(3, positiveIntervals.size());
        final TimeInterval pos1 = new TimeInterval(100, 300);
        final TimeInterval pos2 = new TimeInterval(400, 600);
        final TimeInterval pos3 = new TimeInterval(800, Long.MAX_VALUE);
        assertEquals(pos1, positiveIntervals.get(0));
        assertEquals(pos2, positiveIntervals.get(1));
        assertEquals(pos3, positiveIntervals.get(2));
    }

	@Test
    public void testPositiveIntegrationLinear() {
        final List<SampledValue> values = new ArrayList<>();
        values.add(new SampledValue(new FloatValue(-1.f), 0, Quality.GOOD));
        values.add(new SampledValue(new FloatValue(1.f), 200, Quality.GOOD));
        values.add(new SampledValue(new FloatValue(-1.f), 400, Quality.GOOD));
        FloatTimeSeries f = new FloatTreeTimeSeries();
        f.addValues(values);
        f.setInterpolationMode(InterpolationMode.LINEAR);
        final float positiveIntegral = f.integratePositive(Long.MIN_VALUE, Long.MAX_VALUE);
        assertEquals(100.f, positiveIntegral, 1.f);
        f.multiplyBy(-1.f);
        final float negativeIntegral = f.integratePositive(Long.MIN_VALUE, Long.MAX_VALUE);
        assertEquals(100.f, negativeIntegral, 1.f);        
    }

	@Test
    public void testPositiveIntegrationSteps() {
        final List<SampledValue> values = new ArrayList<>();
        values.add(new SampledValue(new FloatValue(-1.f), 0, Quality.GOOD));
        values.add(new SampledValue(new FloatValue(1.f), 200, Quality.GOOD));
        values.add(new SampledValue(new FloatValue(-1.f), 400, Quality.GOOD));
        FloatTimeSeries f = new FloatTreeTimeSeries();
        f.addValues(values);
        f.setInterpolationMode(InterpolationMode.STEPS);
        final float positiveIntegral = f.integratePositive(Long.MIN_VALUE, 500);
        assertEquals(200.f, positiveIntegral, 1.f);
        f.multiplyBy(-1.f);
        final float negativeIntegral = f.integratePositive(Long.MIN_VALUE, 500);
        assertEquals(300.f, negativeIntegral, 1.f);        
    }
}
