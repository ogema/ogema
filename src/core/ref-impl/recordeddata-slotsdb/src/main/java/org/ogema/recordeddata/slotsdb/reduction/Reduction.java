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

import java.util.List;

import org.ogema.core.channelmanager.measurements.SampledValue;

public interface Reduction {

	/**
	 * Performs the reduction on the given values of the interval.
	 * 
	 * @param intervalValues
	 *            Values
	 * @param timestamp
	 *            of the resulting value
	 * 
	 * @return List of aggregated values. List is never empty contains at least one value. If aggregation was successful
	 *         it holds the Quality.GOOD flag otherwise Quality false.
	 */
	List<SampledValue> performReduction(List<SampledValue> intervalValues, long timestamp);

}
