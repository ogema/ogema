/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3
 * as published by the Free Software Foundation.
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

import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.IntegerValue;
import org.ogema.core.channelmanager.measurements.LongValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.tools.timeseries.api.InterpolationFunction;

/**
 * Step interpolation: Always return a copy of x0.
 */
public class StepInterpolation implements InterpolationFunction {

	@Override
	public SampledValue interpolate(SampledValue x0, SampledValue x1, long t, Class<? extends Value> valueType) {
		if (x0 != null)
			return new SampledValue(x0.getValue(), t, x0.getQuality());
		else if (x1 != null)
			return new SampledValue(x1.getValue(), t, Quality.BAD);
		throw new IllegalArgumentException("Cannot perform step interpolation with both support points being null.");
	}

	@Override
	public Value integrate(SampledValue x0, SampledValue x1, Class<? extends Value> valueType) {
		final long dt = x1.getTimestamp() - x0.getTimestamp();
		final boolean emptyDomain = (dt == 0 || x0.getQuality() == Quality.BAD || x1.getQuality() == Quality.BAD);

		final Value value = x0.getValue();
		if (value instanceof FloatValue) {
			if (emptyDomain)
				return new FloatValue(0.f);
			return new FloatValue(value.getFloatValue() * dt);
		}
		if (value instanceof LongValue) {
			if (emptyDomain)
				return new LongValue(0L);
			return new LongValue(value.getLongValue() * dt);
		}
		if (value instanceof IntegerValue) {
			if (emptyDomain)
				return new IntegerValue(0);
			return new IntegerValue((int) (value.getIntegerValue() * dt));
		}
		throw new IllegalArgumentException("Cannot integrate a function with non-numerical value type "
				+ value.getClass().getCanonicalName());
	}

	//    @Override
	//    public Value integrateAbsolute(SampledValue x0, SampledValue x1, Class<? extends Value> valueType) {
	//		final long dt = x1.getTimestamp() - x0.getTimestamp();
	//		final boolean emptyDomain = (dt == 0 || x0.getQuality() == Quality.BAD || x1.getQuality() == Quality.BAD);
	//
	//		final Value value = x0.getValue();
	//		if (value instanceof FloatValue) {
	//			if (emptyDomain)
	//				return new FloatValue(0.f);
	//			return new FloatValue(Math.abs(value.getFloatValue()) * dt);
	//		}
	//		if (value instanceof LongValue) {
	//			if (emptyDomain)
	//				return new LongValue(0L);
	//			return new LongValue(Math.abs(value.getLongValue()) * dt);
	//		}
	//		if (value instanceof IntegerValue) {
	//			if (emptyDomain)
	//				return new IntegerValue(0);
	//			return new IntegerValue((int) (Math.abs(value.getIntegerValue()) * dt));
	//		}
	//		throw new IllegalArgumentException("Cannot integrate a function with non-numerical value type "
	//				+ value.getClass().getCanonicalName());
	//    }
}
