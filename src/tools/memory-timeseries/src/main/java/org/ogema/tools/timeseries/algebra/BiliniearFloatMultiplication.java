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
package org.ogema.tools.timeseries.algebra;

import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.tools.timeseries.api.BilinearSampledValueOperator;

/**
 * Multiplication of two SampledValues of type FloatValue.
 * 
 * @author Timo Fischer, Fraunhofer IWES
 */
public class BiliniearFloatMultiplication implements BilinearSampledValueOperator {

	public BiliniearFloatMultiplication() {
	}

	@Override
	public final SampledValue apply(final SampledValue value1, final SampledValue value2) {
		final long t1 = value1.getTimestamp();
		final long t2 = value2.getTimestamp();

		if (t1 != t2)
			return new SampledValue(new FloatValue(0.f), t1, Quality.BAD);

		final float x1 = value1.getValue().getFloatValue();
		final float x2 = value2.getValue().getFloatValue();
		final Quality q1 = value1.getQuality();
		final Quality q2 = value2.getQuality();
		final Quality quality = (q1 == Quality.GOOD && q2 == Quality.GOOD) ? Quality.GOOD : Quality.BAD;
		return new SampledValue(new FloatValue(x1 * x2), t2, quality);
	}
}
