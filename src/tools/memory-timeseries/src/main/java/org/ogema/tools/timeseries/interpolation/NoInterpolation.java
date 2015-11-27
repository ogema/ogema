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

import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.StringValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.tools.timeseries.api.InterpolationFunction;
import org.ogema.tools.timeseries.api.TimeInterval;

/**
 * Interpolation modes that performs no interpolation at all. Returns
 * Quality.BAD if requested time for interpolation does not equal one of the two
 * support values.
 */
public class NoInterpolation implements InterpolationFunction {

	@Override
	public SampledValue interpolate(SampledValue x0, SampledValue x1, long t, Class<? extends Value> valueType) {
		if (x0 == null && x1 == null) {
			throw new IllegalArgumentException("Cannot interpolate between two null pointers (x0==null, x1==null).");
		}
		if (x0 != null && x0.getTimestamp() == t) {
			return new SampledValue(x0);
		}
		if (x1 != null && x1.getTimestamp() == t) {
			return new SampledValue(x1);
		}

		return (x0 != null) ? new SampledValue(x0.getValue(), t, Quality.BAD) : new SampledValue(x1.getValue(), t,
				Quality.BAD);
	}

	@Override
	public Value integrate(SampledValue x0, SampledValue x1, Class<? extends Value> valueType) {
		throw new UnsupportedOperationException("Interpolation mode NONE does not allow integration of functions.");
	}

	@Override
	public TimeInterval getPositiveInterval(SampledValue x0, SampledValue x1, Class<? extends Value> valueType) {
		if (StringValue.class.isAssignableFrom(valueType)) {
			throw new RuntimeException("Cannot define positive values for StringValues");
		}
		if (x0 == null || x0.getQuality() == Quality.BAD) {
			return new TimeInterval(0, 0);
		}
		final float value = x0.getValue().getFloatValue();
		if (value <= 0.) {
			return new TimeInterval(0, 0);
		}
		final long t = x0.getTimestamp();
		return new TimeInterval(t, t + 1);
	}
}
