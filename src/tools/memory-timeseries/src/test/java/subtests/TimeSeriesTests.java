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
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * OGEMA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * OGEMA. If not, see <http://www.gnu.org/licenses/>.
 */
package subtests;

/**
 * Copyright 2009 - 2014
 *
 * Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
 *
 * Fraunhofer IIS Fraunhofer ISE Fraunhofer IWES
 *
 * All Rights reserved
 */
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import static org.junit.Assert.assertNull;

import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.core.timeseries.TimeSeries;
import org.ogema.tools.timeseries.api.MemoryTimeSeries;

/**
 * Tests for the functionalities defined in the TimeSeries interface. This is
 * the basis class containing tests valid for all data types.
 *
 * @author Timo Fischer, Fraunhofer IWES
 */
public abstract class TimeSeriesTests {

	protected final MemoryTimeSeries m_timeSeries;

	/**
	 * Gets the value as a float, if possible. Booleans are converted in the
	 * sense of "true"=1, "false"=0.
	 */
	public abstract float getFloat(Value value);

	/**
	 * Returns a Value suitable for the TimeSeries data type
	 *
	 * @param x Float value to be wrapped into a Value container.
	 */
	public abstract Value getValue(float x);

	public static SampledValue getSampledValue(Value value, long t, Quality q) {
		return new SampledValue(value, t, q);
	}

	public TimeSeriesTests(MemoryTimeSeries timeSeries) {
		m_timeSeries = timeSeries;
		testDelete();
	}

	public TimeSeries getTimeSeries() {
		return m_timeSeries;
	}

	private void assertEquals(float x0, float x1, float delta) {
		final boolean okay = (Math.abs(x1 - x0) <= delta);
		if (!okay)
			throw new AssertionError();
	}

	/**
	 * Test the equality of two lists in the sense of their entries being
	 * identical value-wise.
	 */
	void testEquality(List<SampledValue> l1, List<SampledValue> l2) {
		assert l1.size() == l2.size();
		for (int i = 0; i < l1.size(); ++i) {
			SampledValue value1 = l1.get(i);
			SampledValue value2 = l2.get(i);
			assert value1.getTimestamp() == value2.getTimestamp();
			assert getFloat(value1.getValue()) == getFloat(value2.getValue());
			assert value1.getQuality() == value2.getQuality();
		}
	}

	/**
	 * Fill schedule with entries from t0 (inclusive) to t1 (exclusive) with a
	 * time step delta. Function values y are chosen to y=m*t+b.
	 */
	public List<SampledValue> getValuesScheduleLinear(long t0, long t1, long delta, float m, float b) {
        List<SampledValue> result = new ArrayList<>();
        for (long t = t0; t < t1; t += delta) {
            result.add(getSampledValue(getValue(m * t + b), t, Quality.GOOD));
        }
        return result;
    }

	public void performAllTests() {
		testDelete();
		testSetInterpolation();
		testInsertValues();
		testReadWriteTimeSeries();
		testInsertionDeletion();
		testReplaceValueFixedStep();
		testAddValuesWorks();
		testReplaceValuesWorks();
		testIntervalDeletion();
		testGetPreviousValueWorks();
		testIsEmptyAndSizeWork();
	}

	/**
	 * Tests setting all possible interpolation modes. Does not test the actual
	 * interpolation.
	 */
	public void testSetInterpolation() {
		getTimeSeries().setInterpolationMode(InterpolationMode.LINEAR);
		assert InterpolationMode.LINEAR == getTimeSeries().getInterpolationMode();
		getTimeSeries().setInterpolationMode(InterpolationMode.STEPS);
		assert InterpolationMode.STEPS == getTimeSeries().getInterpolationMode();
		getTimeSeries().setInterpolationMode(InterpolationMode.NEAREST);
		assert InterpolationMode.NEAREST == getTimeSeries().getInterpolationMode();
		getTimeSeries().setInterpolationMode(InterpolationMode.NONE);
		assert InterpolationMode.NONE == getTimeSeries().getInterpolationMode();
	}

	/**
	 * Tests the deletion of all values.
	 */
	final void testDelete() {
		m_timeSeries.deleteValues();
		final List<SampledValue> values = m_timeSeries.getValues(0);
		assert values != null;
		assert values.isEmpty();
	}

	/**
	 * Tests the insertion of values.
	 */
	void testInsertValues() {
		testDelete();
		final int N = 10;
		for (int i = 0; i < N; ++i) {
			m_timeSeries.addValue(i, getValue(i));
		}

		assert m_timeSeries.getValue(0) != null;
		assert m_timeSeries.getValues(0).size() == N;
		assert m_timeSeries.getValues(N).isEmpty();

		for (int i = 0; i < N; ++i) {
			m_timeSeries.addValue(N, getValue(i));
		}
		assert m_timeSeries.getValues(N).size() == 1;
	}

	/**
	 * Tests some writing and reading and interpolation
	 */
	void testReadWriteTimeSeries() {

		testDelete();

		// test adding values.
		final long MAX = 100, STEP = 10;
		for (long t = 0; t <= MAX; t += STEP) {
			final Value value = getValue((float) t);
			m_timeSeries.addValue(t, value, t);
		}
		assert m_timeSeries.getLastCalculationTime().longValue() == MAX;

		m_timeSeries.setInterpolationMode(InterpolationMode.LINEAR);

		// test correctness of values, both exact and interpolated
		for (long t = 0; t < MAX; t += STEP) {
			final float exactValue = getFloat(m_timeSeries.getValue(t).getValue());
			final float interpolatedValue = getFloat(m_timeSeries.getValue(t + STEP / 2).getValue());
			assertEquals(exactValue, (float) t, 0.01f);
			assertEquals(interpolatedValue, (float) t + 0.5f * STEP, 0.01f);
		}

		final SampledValue outOfBoundsValue = m_timeSeries.getValue(MAX + 1);
		assertNull(outOfBoundsValue);
	}

	public void testInsertionDeletion() {
		m_timeSeries.deleteValues();

		final long N = 26; // must be an even number for this test.
		final long min = 0, delta = 500, max = min + N * delta;

		List<SampledValue> values, scheduleValues;

		// test inserting an re-reading of data.
		values = getValuesScheduleLinear(min, max, delta, 1.f, 0.f);
		m_timeSeries.addValues(values);

		scheduleValues = m_timeSeries.getValues(min);
		testEquality(values, scheduleValues);

		// test re-inserting different set of values in the same interval.
		values = getValuesScheduleLinear(min, max, 2 * delta, -4.f, 100.f);
		m_timeSeries.replaceValues(min, max, values);
		scheduleValues = m_timeSeries.getValues(min, max);
		testEquality(values, scheduleValues);

		// test emptying the schedule
		m_timeSeries.deleteValues();
		scheduleValues = m_timeSeries.getValues(0);
		assert scheduleValues.isEmpty();
	}
	
	public void testIntervalDeletion() {
		m_timeSeries.deleteValues();
		List<SampledValue> values = getValuesScheduleLinear(-100, 100, 10, 1, 0);
		m_timeSeries.addValues(values);
		assert m_timeSeries.getValues(-40, 40).size() > 0 : "Time series values missing";
		m_timeSeries.deleteValues(-50, 50);
		assert m_timeSeries.getValues(-40, 40).isEmpty() : "Time series contains deleted values";
		assert m_timeSeries.getValues(-100,100).size() > 0 : "Time series values missing";
	}

	public void testReplaceValueFixedStep() {
        m_timeSeries.deleteValues();
        final long N = 73; // must be an even number for this test.
        final long min = 0, delta = 100000, max = min + N * delta;

        final List<Value> values = new ArrayList<>();
        for (int i = 0; i < N; ++i) {
            values.add(getValue(1.f * i));
        }

        m_timeSeries.replaceValuesFixedStep(min, values, delta, 6232);
        assert N == m_timeSeries.getValues(0).size();
        values.add(getValue(652.f));
        m_timeSeries.replaceValuesFixedStep(min, values, delta, 6232);
        assert N + 1 == m_timeSeries.getValues(0).size();
    }

	public void testAddValuesWorks() {
        m_timeSeries.deleteValues();
        List<SampledValue> values1 = new ArrayList<>();
        values1.add(new SampledValue(getValue(1.f), 0, Quality.GOOD));
        values1.add(new SampledValue(getValue(2.f), 1, Quality.GOOD));

        m_timeSeries.addValues(values1);
        List<SampledValue> entries = m_timeSeries.getValues(0);
        testEquality(values1, entries);
    }

	public void testReplaceValuesWorks() {
        m_timeSeries.deleteValues();
        List<SampledValue> values1 = new ArrayList<>();
        values1.add(new SampledValue(getValue(1.f), 0, Quality.GOOD));
        values1.add(new SampledValue(getValue(2.f), 1, Quality.GOOD));
        m_timeSeries.addValues(values1);
        testEquality(values1, m_timeSeries.getValues(0));

        // replace individually
        List<SampledValue> values2 = new ArrayList<>();
        values2.add(new SampledValue(getValue(3.f), 0, Quality.GOOD));
        values2.add(new SampledValue(getValue(4.f), 1, Quality.GOOD));
        Assert.assertFalse(values2.equals(m_timeSeries.getValues(0)));

        for (SampledValue value : values2)
            m_timeSeries.addValue(value.getTimestamp(), value.getValue());
        testEquality(values2, m_timeSeries.getValues(0));

        // replace collectively
        List<SampledValue> values3 = new ArrayList<>();
        values3.add(new SampledValue(getValue(3.f), 0, Quality.GOOD));
        values3.add(new SampledValue(getValue(4.f), 1, Quality.GOOD));

        m_timeSeries.addValues(values3);
        testEquality(values3, m_timeSeries.getValues(0));
    }

	public void testShiftTimestampsWorks() {
        m_timeSeries.deleteValues();
        List<SampledValue> values1 = new ArrayList<>();
        values1.add(new SampledValue(getValue(1.f), 0, Quality.GOOD));
        values1.add(new SampledValue(getValue(2.f), 100, Quality.GOOD));

        m_timeSeries.addValues(values1);
        List<SampledValue> entries = m_timeSeries.getValues(0);
        testEquality(values1, entries);

        m_timeSeries.shiftTimestamps(-100);
        List<SampledValue> shiftedEntries = m_timeSeries.getValues(0);
        assert (shiftedEntries.size() == 1);
        SampledValue shiftedValue = shiftedEntries.get(0);
        assertEquals(getFloat(shiftedValue.getValue()), 2.f, 0.01f);
    }
	
	public void testGetPreviousValueWorks() {
		m_timeSeries.deleteValues();
		SampledValue sv = m_timeSeries.getPreviousValue(Long.MAX_VALUE);
		Assert.assertNull(sv);
		SampledValue sv1 = new SampledValue(new FloatValue(34), 10, Quality.GOOD);
		SampledValue sv2 = new SampledValue(new FloatValue(52), 20, Quality.GOOD);
		SampledValue sv3 = new SampledValue(new FloatValue(100), 30, Quality.BAD);
		m_timeSeries.addValue(sv1);
		m_timeSeries.addValue(sv2);
		m_timeSeries.addValue(sv3);
		sv = m_timeSeries.getPreviousValue(25);
		Assert.assertEquals(sv2, sv);
		sv = m_timeSeries.getPreviousValue(Long.MAX_VALUE);
		Assert.assertEquals(sv3, sv);
		sv = m_timeSeries.getPreviousValue(0);
		Assert.assertNull(sv);
	}
	
	public void testIsEmptyAndSizeWork() {
		m_timeSeries.deleteValues();
		Assert.assertTrue(m_timeSeries.isEmpty());
		Assert.assertTrue(m_timeSeries.isEmpty(Long.MIN_VALUE, Long.MAX_VALUE));
		Assert.assertEquals(0, m_timeSeries.size());
		Assert.assertEquals(0, m_timeSeries.size(Long.MIN_VALUE, Long.MAX_VALUE));
		
		long start = 0;
		long end = 1000;
		
		List<SampledValue> values = getValuesScheduleLinear(start, end, 5, 2, 1);
		final int size = values.size();
		m_timeSeries.addValues(values);
		
		Assert.assertFalse(m_timeSeries.isEmpty());
		Assert.assertFalse(m_timeSeries.isEmpty(Long.MIN_VALUE, Long.MAX_VALUE));
		Assert.assertEquals(size, m_timeSeries.size());
		Assert.assertEquals(size, m_timeSeries.size(Long.MIN_VALUE, Long.MAX_VALUE));
		
		Assert.assertTrue(m_timeSeries.isEmpty(Long.MIN_VALUE, start-1));
		Assert.assertEquals(0, m_timeSeries.size(Long.MIN_VALUE, start-1));
		Assert.assertTrue(m_timeSeries.isEmpty(end+1,Long.MAX_VALUE));
		Assert.assertEquals(0, m_timeSeries.size(end+1,Long.MAX_VALUE));
		
		Assert.assertFalse(m_timeSeries.isEmpty(100,200));
		Assert.assertEquals(20, m_timeSeries.size(100,199));
		
	}
}
