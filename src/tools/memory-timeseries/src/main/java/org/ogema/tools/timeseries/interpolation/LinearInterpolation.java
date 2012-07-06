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
package org.ogema.tools.timeseries.interpolation;

import org.ogema.core.channelmanager.measurements.BooleanValue;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.IntegerValue;
import org.ogema.core.channelmanager.measurements.LongValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.tools.timeseries.api.InterpolationFunction;

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

		if (v0 instanceof FloatValue) {
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

	//    @Override
	//    public Value integrateAbsolute(SampledValue x0, SampledValue x1, Class<? extends Value> valueType) {
	//        final long dt = x1.getTimestamp() - x0.getTimestamp();
	//        final boolean emptyDomain = (dt == 0 || x0.getQuality() == Quality.BAD || x1.getQuality() == Quality.BAD);
	//
	//        final Value v0 = x0.getValue();
	//        final Value v1 = x1.getValue();
	//
	//        if (v0 instanceof FloatValue) {
	//            if (emptyDomain) return new FloatValue(0.f);
	//            if (dt < 0) {
	//                final FloatValue negResult = (FloatValue) integrateAbsolute(x1, x0, valueType);
	//                return new FloatValue(-negResult.getFloatValue());
	//            }
	//
	//            final float f0 = v0.getFloatValue();
	//            final float f1 = v1.getFloatValue();
	//            if (f0 * f1 >= 0) { // no zero-crossing.
	//                return new FloatValue(0.5f * Math.abs(v0.getFloatValue() + v1.getFloatValue()) * dt);
	//            }
	//            final float slope = Math.abs(f1 - f0) / (float) dt;
	//            final float A0 = 0.5f * f0 * f0 / slope;
	//            final float A1 = 0.5f * f1 * f1 / slope;
	//            return new FloatValue(A0 + A1);
	//        }
	//
	//        if (v0 instanceof LongValue) {
	//            if (emptyDomain) return new LongValue(0L);
	//            if (dt < 0) {
	//                final LongValue negResult = (LongValue) integrateAbsolute(x1, x0, valueType);
	//                return new LongValue(-negResult.getLongValue());
	//            }
	//
	//            final float f0 = v0.getFloatValue();
	//            final float f1 = v1.getFloatValue();
	//            if (f0 * f1 >= 0) { // no zero-crossing.
	//                return new FloatValue(0.5f * Math.abs(v0.getFloatValue() + v1.getFloatValue()) * dt);
	//            }
	//            final float slope = Math.abs(f1 - f0) / (float) dt;
	//            final float A0 = 0.5f * f0 * f0 / slope;
	//            final float A1 = 0.5f * f1 * f1 / slope;
	//            return new LongValue( (long) (A0 + A1));
	//        }
	//        
	//        if (v0 instanceof IntegerValue) {
	//            if (emptyDomain) return new IntegerValue(0);
	//            if (dt < 0) {
	//                final IntegerValue negResult = (IntegerValue) integrateAbsolute(x1, x0, valueType);
	//                return new IntegerValue(-negResult.getIntegerValue());
	//            }
	//
	//            final float f0 = v0.getFloatValue();
	//            final float f1 = v1.getFloatValue();
	//            if (f0 * f1 >= 0) { // no zero-crossing.
	//                return new FloatValue(0.5f * Math.abs(v0.getFloatValue() + v1.getFloatValue()) * dt);
	//            }
	//            final float slope = Math.abs(f1 - f0) / (float) dt;
	//            final float A0 = 0.5f * f0 * f0 / slope;
	//            final float A1 = 0.5f * f1 * f1 / slope;
	//            return new IntegerValue( (int) (A0 + A1));            
	//        }
	//        
	//        throw new IllegalArgumentException("Cannot integrate a function with non-numerical value type "
	//                + v0.getClass().getCanonicalName());
	//    }
}
