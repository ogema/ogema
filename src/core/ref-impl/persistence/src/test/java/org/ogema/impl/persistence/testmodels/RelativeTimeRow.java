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
package org.ogema.impl.persistence.testmodels;

import org.ogema.core.model.array.FloatArrayResource;
import org.ogema.core.model.array.TimeArrayResource;
import org.ogema.model.prototypes.Data;

/**
 * Vector of float values that represent a curve over time. This resource type shall be used when the beginning of the
 * curve is variable, so the first time stamp should be zero and all time stamps are considered relative to the starting
 * time. The last time stamp is considered the end of the curve, so the last step of the curve has duration zero. If
 * this is not intended the last two values of {@link values} should be the same with the last two time stamps defining
 * the duration of the last step of the curve over time.
 */
public interface RelativeTimeRow extends Data {
	/**
	 * time stamps indicating the beginning of the period for which the respective value is valid
	 */
	public TimeArrayResource timeStamps();

	/** vector of values */
	public FloatArrayResource values();
}
