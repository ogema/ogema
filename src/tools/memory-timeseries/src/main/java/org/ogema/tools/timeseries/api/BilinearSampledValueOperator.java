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
package org.ogema.tools.timeseries.api;

import org.ogema.core.channelmanager.measurements.SampledValue;

/**
 * An operation V x V -> V over SampledValues V that is linear in both arguments. Examples
 * are addition and multiplication of two values.
 */
public interface BilinearSampledValueOperator {

	/**
	 * Performs the operation and returns the result. Arguments are constant. The
	 * quality of the result is good if the quality of both inputs was good and if
	 * both inputs have the same timestamp. Otherwise, the quality of the result is bad.  
	 */
	SampledValue apply(final SampledValue value1, final SampledValue value2);
}
