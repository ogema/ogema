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

import org.ogema.core.recordeddata.ReductionMode;

public class ReductionFactory {

	public Reduction getReduction(ReductionMode mode) {

		//NOTE: NONE is not considered here since no reduction is needed. 
		switch (mode) {
		case AVERAGE:
			return new AverageReduction();
		case MAXIMUM_VALUE:
			return new MaximumReduction();
		case MIN_MAX_VALUE:
			return new MinMaxReduction();
		case MINIMUM_VALUE:
			return new MinimumReduction();
		default:
			throw new IllegalArgumentException("Mode " + mode.toString() + " not supported yet");
		}

	}

}
