/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
 */
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

/**
 * Tests for the implementation FloatTreeTimeSeries, that implements the FloatTimeSeries
 * interface which in turn defines an algebra on float-valued time series.
 * 
 * TODO add tests for the algebra
 * 
 * @author Timo Fischer, Fraunhofer IWES
 */
public class FloatTreeTimeSeriesTests {

	/**
	 * Creates an equidistant FloatTimeSeries over [t0; t0+dt; ... t1) with values
	 * equal to x = a*t+b and set its interpolation mode.
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
	 * Tests the basic methods of a time series that are not associated to the algebra.
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
}
