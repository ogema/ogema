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
package org.ogema.recordeddata.slotsdb.reduction;

import java.util.ArrayList;
import java.util.List;

import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.recordeddata.slotsdb.DoubleValues;

public class MaximumReduction implements Reduction {

	@Override
	public List<SampledValue> performReduction(List<SampledValue> subIntervalValues, long timestamp) {

		List<SampledValue> toReturn = new ArrayList<SampledValue>();

		if (subIntervalValues.isEmpty()) {
			toReturn.add(new SampledValue(DoubleValues.of(0.f), timestamp, Quality.BAD));
		}
		else {
			double maxValue = Double.NEGATIVE_INFINITY;
			for (SampledValue value : subIntervalValues) {
				if (value.getValue().getDoubleValue() > maxValue) {
					maxValue = value.getValue().getDoubleValue();
				}
			}
			toReturn.add(new SampledValue(new SampledValue(DoubleValues.of(maxValue), timestamp, Quality.GOOD)));
		}

		return toReturn;
	}

}
