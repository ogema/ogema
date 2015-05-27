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
package org.ogema.recordeddata.slotsdb.reduction;

import java.util.ArrayList;
import java.util.List;

import org.ogema.core.channelmanager.measurements.DoubleValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;

public class MaximumReduction implements Reduction {

	@Override
	public List<SampledValue> performReduction(List<SampledValue> subIntervalValues, long timestamp) {

		List<SampledValue> toReturn = new ArrayList<SampledValue>();

		if (subIntervalValues.isEmpty()) {
			toReturn.add(new SampledValue(new DoubleValue(0.f), timestamp, Quality.BAD));
		}
		else {
			double maxValue = Double.NEGATIVE_INFINITY;
			for (SampledValue value : subIntervalValues) {
				if (value.getValue().getDoubleValue() > maxValue) {
					maxValue = value.getValue().getDoubleValue();
				}
			}
			toReturn.add(new SampledValue(new SampledValue(new DoubleValue(maxValue), timestamp, Quality.GOOD)));
		}

		return toReturn;
	}

}
