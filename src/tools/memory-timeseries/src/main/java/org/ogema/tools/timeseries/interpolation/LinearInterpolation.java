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
package org.ogema.tools.timeseries.interpolation;

import org.ogema.core.channelmanager.measurements.BooleanValue;
import org.ogema.core.channelmanager.measurements.DoubleValue;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.IntegerValue;
import org.ogema.core.channelmanager.measurements.LongValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.tools.timeseries.api.InterpolationFunction;
import org.ogema.tools.timeseries.api.TimeInterval;

/**
 * Linear interpolation between the points.
 */
public class LinearInterpolation implements InterpolationFunction {

	@Override
	public SampledValue interpolate(SampledValue x0, SampledValue x1, long t, Class<? extends Value> valueType) {
		if (x0 == null && x1 == null) {
			throw new IllegalArgumentException("Cannot interpolate between two null pointers (x0==null, x1==null).");
		}
		try {
			if (x0 == null)
				return new SampledValue(x1.getValue().clone(), t, Quality.BAD);
			if (x1 == null)
				return new SampledValue(x0.getValue().clone(), t, Quality.BAD);
		} catch (CloneNotSupportedException ex) {
			return null;
		}

		if (valueType == FloatValue.class) {
			return interpolateFloat(x0, x1, t);
		}
		if (valueType == IntegerValue.class) {
			return interpolateInt(x0, x1, t);
		}
		if (valueType == BooleanValue.class) {
			return interpolateBoolean(x0, x1, t);
		}
		if (valueType == LongValue.class) {
			return interpolateLong(x0, x1, t);
		}
		// no linear interpolation for Strings, for example.
		throw new UnsupportedOperationException(valueType.getCanonicalName()
				+ " not supported for linear interpolation.");

	}

	private SampledValue interpolateFloat(SampledValue x0, SampledValue x1, long t) {
		final long t0 = x0.getTimestamp();
		if (t == t0) {
			return new SampledValue(x0.getValue(), x0.getTimestamp(), x0.getQuality());
		}

		final long t1 = x1.getTimestamp();
		if (t == t1) {
			return new SampledValue(x1.getValue(), x1.getTimestamp(), x1.getQuality());
		}

		final float y0 = x0.getValue().getFloatValue();
		final float y1 = x1.getValue().getFloatValue();
		final float inv_dt = 1.f / (float) (t1 - t0);
		final float relDist = ((float) (t - t0)) * inv_dt;
		final float y = y0 + relDist * (y1 - y0);
		final Quality q0 = x0.getQuality();
		final Quality q1 = x1.getQuality();
		final Quality quality = (q0 == Quality.GOOD && q1 == Quality.GOOD) ? Quality.GOOD : Quality.BAD;

		return new SampledValue(new FloatValue(y), t, quality);
	}

	private SampledValue interpolateInt(SampledValue x0, SampledValue x1, long t) {
		final long t0 = x0.getTimestamp();
		if (t == t0) {
			return new SampledValue(x0.getValue(), x0.getTimestamp(), x0.getQuality());
		}

		final long t1 = x1.getTimestamp();
		if (t == t1) {
			return new SampledValue(x1.getValue(), x1.getTimestamp(), x1.getQuality());
		}

		final int y0 = x0.getValue().getIntegerValue();
		final int y1 = x1.getValue().getIntegerValue();
		final float inv_dt = 1.f / (float) (t1 - t0);
		final float relDist = ((float) (t - t0)) * inv_dt;
		final int y = y0 + (int) (relDist * (y1 - y0));
		final Quality q0 = x0.getQuality();
		final Quality q1 = x1.getQuality();
		final Quality quality = (q0 == Quality.GOOD && q1 == Quality.GOOD) ? Quality.GOOD : Quality.BAD;

		return new SampledValue(new IntegerValue(y), t, quality);
	}

	private SampledValue interpolateLong(SampledValue x0, SampledValue x1, long t) {
		final long t0 = x0.getTimestamp();
		if (t == t0) {
			return new SampledValue(x0.getValue(), x0.getTimestamp(), x0.getQuality());
		}

		final long t1 = x1.getTimestamp();
		if (t == t1) {
			return new SampledValue(x1.getValue(), x1.getTimestamp(), x1.getQuality());
		}

		final long y0 = x0.getValue().getLongValue();
		final long y1 = x1.getValue().getLongValue();
		final float inv_dt = 1.f / (float) (t1 - t0);
		final float relDist = ((float) (t - t0)) * inv_dt;
		final long y = y0 + (long) (relDist * (y1 - y0));
		final Quality q0 = x0.getQuality();
		final Quality q1 = x1.getQuality();
		final Quality quality = (q0 == Quality.GOOD && q1 == Quality.GOOD) ? Quality.GOOD : Quality.BAD;

		return new SampledValue(new LongValue(y), t, quality);
	}

	private SampledValue interpolateBoolean(SampledValue x0, SampledValue x1, long t) {
		// linear interpolation on booleans is the same as nearest interpolation 
		final long dt0 = Math.abs(t - x0.getTimestamp());
		final long dt1 = Math.abs(t - x1.getTimestamp());
		if (dt0 <= dt1)
			return new SampledValue(x0.getValue(), t, x0.getQuality());
		else
			return new SampledValue(x1.getValue(), t, x1.getQuality());
	}

	@Override
	public Value integrate(SampledValue x0, SampledValue x1, Class<? extends Value> valueType) {
		final long dt = x1.getTimestamp() - x0.getTimestamp();
		final boolean emptyDomain = (dt == 0 || x0.getQuality() == Quality.BAD || x1.getQuality() == Quality.BAD);

		final Value v0 = x0.getValue();
		final Value v1 = x1.getValue();

		if (v0 instanceof FloatValue || v0 instanceof DoubleValue || v0 instanceof BooleanValue) {
			if (emptyDomain)
				return new FloatValue(0.f);
			return new FloatValue(0.5f * (v0.getFloatValue() + v1.getFloatValue()) * dt);
		}

		if (v0 instanceof LongValue) {
			if (emptyDomain)
				return new LongValue(0L);
			return new LongValue((v0.getLongValue() + v1.getLongValue()) * dt / 2);
		}
		if (v0 instanceof IntegerValue) {
			if (emptyDomain)
				return new IntegerValue(0);
			return new IntegerValue((int) ((v0.getIntegerValue() + v1.getIntegerValue()) * dt / 2));
		}
		throw new IllegalArgumentException("Cannot integrate a function with non-numerical value type "
				+ v0.getClass().getCanonicalName());
	}

	@Override
	public TimeInterval getPositiveInterval(SampledValue s1, SampledValue s2, Class<? extends Value> valueType) {
		if (!FloatValue.class.isAssignableFrom(valueType)) {
			throw new RuntimeException("Method only supported for float values so far.");
		}
		if (s1 == null || s2 == null) {
			return new TimeInterval(0, 0);
		}
		final Quality q1 = s1.getQuality();
		final Quality q2 = s2.getQuality();
		if (q1 == Quality.BAD || q2 == Quality.BAD) {
			return new TimeInterval(0, 0);
		}
		final float x1 = s1.getValue().getFloatValue();
		final float x2 = s2.getValue().getFloatValue();
		if (x1 <= 0. && x2 <= 0.) {
			return new TimeInterval(0, 0);
		}
		final long t1 = s1.getTimestamp();
		final long t2 = s2.getTimestamp();
		if (x1 * x2 < 0.f) {
			final float slope = (x2 - x1) / (float) (t2 - t1);
			// xLast + slope*delta = 0 => delta = -xLast/slope
			final float delta = -x1 / slope;
			final long tMid = t1 + (long) delta;
			if (x1 < 0.f) {
				return new TimeInterval(tMid, t2);
			}
			else {
				return new TimeInterval(t1, tMid);
			}
		}
		else {
			return new TimeInterval(t1, t2);
		}
	}
}
