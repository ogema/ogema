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
import org.ogema.recordeddata.slotsdb.SlotsDb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AverageReduction implements Reduction {

	@Override
	public List<SampledValue> performReduction(List<SampledValue> subIntervalValues, long timestamp) {

		List<SampledValue> toReturn = new ArrayList<SampledValue>();

		if (subIntervalValues.isEmpty()) {
			toReturn.add(new SampledValue(new DoubleValue(0.f), timestamp, Quality.BAD));
		}
		else {
			double sum = 0;
			for (SampledValue value : subIntervalValues) {
				sum += value.getValue().getDoubleValue();
			}

			double average = sum / subIntervalValues.size();
			toReturn.add(new SampledValue(new SampledValue(new DoubleValue(average), timestamp, Quality.GOOD)));
		}

		return toReturn;
	}

}
