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

import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.tools.timeseries.api.InterpolationFunction;
import org.ogema.tools.timeseries.api.TimeInterval;

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

	@Override
	public TimeInterval getPositiveInterval(SampledValue x0, SampledValue x1, Class<? extends Value> valueType) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
}
