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
import org.ogema.core.channelmanager.measurements.StringValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.tools.timeseries.api.InterpolationFunction;
import org.ogema.tools.timeseries.api.TimeInterval;

/**
 * Step interpolation: Always return a copy of x0.
 */
public class StepInterpolation implements InterpolationFunction {

	@Override
	public SampledValue interpolate(SampledValue x0, SampledValue x1, long t, Class<? extends Value> valueType) {
		if (x0 != null) {
			return new SampledValue(x0.getValue(), t, x0.getQuality());
		}
		else if (x1 != null) {
			return new SampledValue(x1.getValue(), t, Quality.BAD);
		}
		throw new IllegalArgumentException("Cannot perform step interpolation with both support points being null.");
	}

	@Override
	public Value integrate(SampledValue x0, SampledValue x1, Class<? extends Value> valueType) {
		final long dt = x1.getTimestamp() - x0.getTimestamp();
		final boolean emptyDomain = (dt == 0 || x0.getQuality() == Quality.BAD || x1.getQuality() == Quality.BAD);

		final Value value = x0.getValue();
		if (value instanceof FloatValue || value instanceof DoubleValue || value instanceof BooleanValue) {
			if (emptyDomain) {
				return new FloatValue(0.f);
			}
			return new FloatValue(value.getFloatValue() * dt);
		}
		if (value instanceof LongValue) {
			if (emptyDomain) {
				return new LongValue(0L);
			}
			return new LongValue(value.getLongValue() * dt);
		}
		if (value instanceof IntegerValue) {
			if (emptyDomain) {
				return new IntegerValue(0);
			}
			return new IntegerValue((int) (value.getIntegerValue() * dt));
		}
		throw new IllegalArgumentException("Cannot integrate a function with non-numerical value type "
				+ value.getClass().getCanonicalName());
	}

	@Override
	public TimeInterval getPositiveInterval(SampledValue x0, SampledValue x1, Class<? extends Value> valueType) {
		if (StringValue.class.isAssignableFrom(valueType)) {
			throw new RuntimeException("Cannot define positive values for StringValues");
		}
		if (x0 == null || x1 == null) {
			return new TimeInterval(0, 0);
		}
		if (x0.getTimestamp() > x1.getTimestamp()) {
			return getPositiveInterval(x1, x0, valueType);
		}
		if (x0.getQuality() == Quality.BAD) {
			return new TimeInterval(0, 0);
		}
		final float value = x0.getValue().getFloatValue();
		if (value <= 0.) {
			return new TimeInterval(0, 0);
		}
		return new TimeInterval(x0.getTimestamp(), x1.getTimestamp());
	}
}
