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

import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.tools.timeseries.api.InterpolationFunction;

/**
 * Intepolation mode: Return the data point that is closest in time. If both are
 * equally distant in time, a copy of x0 is returned.
 */
public class NearestInterpolation implements InterpolationFunction {

	@Override
	public SampledValue interpolate(SampledValue x0, SampledValue x1, long t, Class<? extends Value> valueType) {
		if (x0 == null && x1 == null) {
			throw new IllegalArgumentException("Cannot interpolate between two null pointers (x0==null, x1==null).");
		}
		if (x1 == null) {
			return new SampledValue(x0.getValue(), t, x0.getQuality());
		}
		if (x0 == null) {
			return new SampledValue(x1.getValue(), t, x1.getQuality());
		}
		final long dt0 = Math.abs(t - x0.getTimestamp());
		final long dt1 = Math.abs(t - x1.getTimestamp());
		if (dt0 <= dt1)
			return new SampledValue(x0.getValue(), t, x0.getQuality());
		else
			return new SampledValue(x1.getValue(), t, x1.getQuality());
	}

	@Override
	public Value integrate(SampledValue x0, SampledValue x1, Class<? extends Value> valueType) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	//    @Override
	//    public Value integrateAbsolute(SampledValue x0, SampledValue x1, Class<? extends Value> valueType) {
	//        throw new UnsupportedOperationException("Not supported yet.");
	//    }
}
