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
package org.ogema.tools.timeseries.implementations;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.core.timeseries.ReadOnlyTimeSeries;
import org.ogema.tools.timeseries.algebra.BilinearFloatAddition;
import org.ogema.tools.timeseries.algebra.BiliniearFloatMultiplication;
import org.ogema.tools.timeseries.algebra.ConstantFloatAddition;
import org.ogema.tools.timeseries.algebra.ConstantFloatMultiplication;
import org.ogema.tools.timeseries.api.BilinearSampledValueOperator;
import org.ogema.tools.timeseries.api.FloatTimeSeries;
import org.ogema.tools.timeseries.api.InterpolationFunction;
import org.ogema.tools.timeseries.api.LinearSampledValueOperator;
import org.ogema.tools.memoryschedules.tools.TimeSeriesMerger;

/**
 * Implementation for the FloatTimeSeries internally using a tree structure for
 * storing data.
 * 
 * @author Timo Fischer, Fraunhofer IWES
 */
public class FloatTreeTimeSeries extends TreeTimeSeries implements FloatTimeSeries {

	public FloatTreeTimeSeries() {
		super(FloatValue.class);
	}

	/**
	 * Creates a series that has the constant value for all times.
	 */
	public FloatTreeTimeSeries(float value) {
		super(FloatValue.class);
		setConstant(value);
	}

	/**
	 * Copy constructor.
	 */
	public FloatTreeTimeSeries(FloatTimeSeries other) {
		super(other.getValueType());
		synchronized (other) {
			addValues(other.getValues(Long.MIN_VALUE));
			setInterpolationMode(other.getInterpolationMode());
		}
	}

	/**
	 * Apply the bi-linear operator on all points of this. Second operator input is taken from values.
	 * Store the values in this.
	 */
	protected void applyBilinearOperator(BilinearSampledValueOperator operator, FloatTimeSeries values) {
        final FloatTimeSeries factors = new FloatTreeTimeSeries(values); // hidden double-synchronization.
        final TimeSeriesMerger merger = new TimeSeriesMerger(this, factors);

        // calculate the new values for this.
        final List<SampledValue> newValues = new ArrayList<>(merger.getTimestamps().size());
        for (Long t : merger.getTimestamps()) {
            final SampledValue v1 = this.getValue(t);
            final SampledValue v2 = factors.getValue(t);
            final SampledValue newValue = operator.apply(v1, v2);
            newValues.add(newValue);
        }

        // replace values in this with new values and set interpolation mode.
        deleteValues();
        addValues(newValues);
        setInterpolationMode(merger.getInterpolationMode());
    }

	/**
	 * Apply an operator V->V to all points in this time series.
	 * @param factor 
	 */
	protected void applyLinearOperator(LinearSampledValueOperator operator) {
        final SortedSet<SampledValue> values = getValues();
        final List<SampledValue> newValues = new ArrayList<>(values.size());
        for (SampledValue value : values) {
            final SampledValue newValue = operator.apply(value);
            newValues.add(newValue);
        }
        deleteValues();
        addValues(newValues);
    }

	@Override
	public synchronized void multiplyBy(float factor) {
		LinearSampledValueOperator operator = new ConstantFloatMultiplication(factor);
		applyLinearOperator(operator);
	}

	@Override
	public synchronized void add(float addend) {
		LinearSampledValueOperator operator = new ConstantFloatAddition(addend);
		applyLinearOperator(operator);
	}

	@Override
	public synchronized void add(FloatTimeSeries addends) {
		final BilinearSampledValueOperator operator = new BilinearFloatAddition();
		applyBilinearOperator(operator, addends);
	}

	@Override
	public FloatTreeTimeSeries read(ReadOnlyTimeSeries schedule) {
		return (FloatTreeTimeSeries) super.read(schedule);
	}

	@Override
	public FloatTreeTimeSeries read(ReadOnlyTimeSeries schedule, long start, long end) {
		return (FloatTreeTimeSeries) super.read(schedule, start, end);
	}

	@Override
	public synchronized void multiplyBy(FloatTimeSeries factor) {
		final BilinearSampledValueOperator operator = new BiliniearFloatMultiplication();
		applyBilinearOperator(operator, factor);
	}

	@Override
	public FloatTimeSeries plus(float addend) {
		final FloatTimeSeries result = new FloatTreeTimeSeries(this);
		result.add(addend);
		return result;
	}

	@Override
	public FloatTimeSeries plus(FloatTimeSeries other) {
		final FloatTreeTimeSeries result = new FloatTreeTimeSeries(this);
		result.add(other);
		return result;
	}

	@Override
	public FloatTimeSeries times(float factor) {
		final FloatTreeTimeSeries result = new FloatTreeTimeSeries(this);
		result.multiplyBy(factor);
		return result;
	}

	@Override
	public FloatTimeSeries times(FloatTimeSeries other) {
		final FloatTreeTimeSeries result = new FloatTreeTimeSeries(this);
		result.multiplyBy(other);
		return result;
	}

	@Override
	public synchronized float integrate(long t0, long t1) {

		// react to zero range or t1<t0.
		if (t1 == t0)
			return 0.f;
		if (t1 < t0)
			return -integrate(t1, t0);

		final InterpolationFunction function = getInterpolationFunction();

		double result = 0;
		SampledValue left = getValue(t0);
		for (SampledValue right : getSubset(t0, t1)) {
			result += function.integrate(left, right, getValueType()).getDoubleValue();
			left = right;
		}
		result += function.integrate(left, getValue(t1), getValueType()).getDoubleValue();

		return (float) result;
	}

	@Override
	public synchronized SampledValue getMax(long t0, long t1) {
		final long dt = t1 - t0;
		if (dt <= 0)
			return new SampledValue(new FloatValue(0.f), t0, Quality.BAD);

		final List<SampledValue> values = getValues(t0, t1);
		values.add(getValue(t0));
		if (dt > 1)
			values.add(getValue(t1 - 1));

		if (getInterpolationMode() == InterpolationMode.NEAREST) {
			// Problem: would have to set the timestamp between the support points
			throw new UnsupportedOperationException("Case InterpolationMode.NEAREST is not yet implemented for getMax");
		}

		// result values.
		float max = Float.MIN_VALUE;
		long t = t0;
		Quality q = Quality.BAD;

		for (SampledValue value : values) {
			if (value.getQuality() == Quality.BAD)
				continue;
			final float x = value.getValue().getFloatValue();
			if (x > max) {
				max = x;
				t = value.getTimestamp();
				q = value.getQuality();
			}
		}

		return new SampledValue(new FloatValue(max), t, q);
	}

	@Override
	public synchronized SampledValue getMin(long t0, long t1) {
		final FloatTreeTimeSeries f = new FloatTreeTimeSeries(this);
		f.multiplyBy(-1.f);
		final SampledValue negMax = f.getMax(t0, t1);
		return new SampledValue(new FloatValue(negMax.getValue().getFloatValue()), negMax.getTimestamp(), negMax
				.getQuality());
	}

	/**
	 * Sets the values of the result such that the resulting function is a point-wise
	 * absolute magnitude copy of this.
	 */
	private FloatTimeSeries getAbsoluteLinear(final FloatTimeSeries result) {
		result.setInterpolationMode(InterpolationMode.LINEAR);
		SampledValue lastValue = getValues().first();
		for (SampledValue value : getValues()) {

			// shortcuts for last and this value's properties
			final float xLast = lastValue.getValue().getFloatValue();
			final long tLast = lastValue.getTimestamp();
			final Quality qLast = lastValue.getQuality();
			final float x = value.getValue().getFloatValue();
			final long t = value.getTimestamp();
			final Quality q = value.getQuality();

			if (Float.isNaN(x)) {
				throw new RuntimeException("Could not get the absolute of a function: An entry is NaN.");
			}

			// if this and the last value differ in sign, add an intermediate value.
			if (xLast * x < 0.) {
				final Quality qMid = (q == Quality.GOOD && qLast == Quality.GOOD) ? Quality.GOOD : Quality.BAD;

				final float slope = (x - xLast) / (float) (t - tLast);
				// xLast + slope*delta = 0 => delta = xLast/slope
				final float delta = xLast / slope;
				final long tMid = tLast + (long) delta;
				if (tMid != tLast && tMid != t)
					result.addValue(new SampledValue(new FloatValue(0.f), tMid, qMid));
			}

			// add the absolute of the current value
			result.addValue(new SampledValue(new FloatValue(Math.abs(x)), t, q));

			lastValue = value;
		}
		return result;
	}

	@Override
	public FloatTimeSeries getAbsolute() {
		final FloatTimeSeries result = new FloatTreeTimeSeries();
		result.setInterpolationMode(getInterpolationMode());
		if (getValues().isEmpty())
			return result;

		if (getInterpolationMode() == InterpolationMode.LINEAR) {
			return getAbsoluteLinear(result);
		}
		for (SampledValue value : getValues()) {
			final float x = value.getValue().getFloatValue();
			if (x >= 0) {
				result.addValue(value);
			}
			else {
				result.addValue(new SampledValue(new FloatValue(-x), value.getTimestamp(), value.getQuality()));
			}
		}
		return result;
	}

	@Override
	public float integrateAbsolute(long t0, long t1) {
		final FloatTimeSeries absCopy = getAbsolute();
		return absCopy.integrate(t0, t1);
	}

	@Override
	public final void setConstant(float value) {
		deleteValues();
		setInterpolationMode(InterpolationMode.STEPS);
		addValue(Long.MIN_VALUE, new FloatValue(value));
		addValue(0, new FloatValue(value));
	}

	@Override
	public void optimizeRepresentation() {

	}
}
