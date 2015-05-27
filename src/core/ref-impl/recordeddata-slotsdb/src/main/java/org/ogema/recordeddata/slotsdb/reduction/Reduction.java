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

import java.util.List;

import org.ogema.core.channelmanager.measurements.SampledValue;

public interface Reduction {

	/**
	 * Performs the reduction on the given values of the interval.
	 * 
	 * @param subIntervalValues
	 *            Values
	 * @param timestamp
	 *            of the resulting value
	 * 
	 * @return List of aggregated values. List is never empty contains at least one value. If aggregation was successful
	 *         it holds the Quality.GOOD flag otherwise Quality false.
	 */
	List<SampledValue> performReduction(List<SampledValue> intervalValues, long timestamp);

}
