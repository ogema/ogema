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
package org.ogema.tools.timeseries.algebra;

import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.tools.timeseries.api.BilinearSampledValueOperator;

/**
 * Addition of two SampledValues of type FloatValue.
 * 
 * @author Timo Fischer, Fraunhofer IWES
 */
public class BilinearFloatAddition implements BilinearSampledValueOperator {

	public BilinearFloatAddition() {
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
		return new SampledValue(new FloatValue(x1 + x2), t2, quality);
	}
}
