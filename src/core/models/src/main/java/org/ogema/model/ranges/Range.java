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
package org.ogema.model.ranges;

import org.ogema.core.model.ValueResource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.model.prototypes.Data;

/**
 * Prototype definition of range limits. This should not be used in real data models, but be extended
 * by particular ranges. By default, the range defined in this model is to be understood as including the
 * limits. If this shall not be the case,  {@link #upperLimitIncluded()} and {@link #lowerLimitIncluded()} 
 * may be set accordingly to change this.
 */
public interface Range extends Data {
	/**
	 * Upper range threshold. If this is not set, the range is unlimited towards the upper end.
	 * Unit: Defined in the model extending this prototype.
	 */
	ValueResource upperLimit();

	/**
	 * Lower range threshold. If this is not set, the range is unlimited towards the lower end.
	 * Unit: Defined in the model extending this prototype.
	 */
	ValueResource lowerLimit();

	/**
	 * If true the upper limit is considered part of the range, otherwise the upper limit value is considered outside
	 * the range. If not set, the upper limit is included. <br>
	 * Note that applications are not required to take care of this flag as for most use cases this is not relevant.
	 */
	BooleanResource upperLimitIncluded();

	/**
	 * If true the lower limit is considered part of the range, otherwise the lower limit value is considered outside
	 * the range. If not set, the lower limit is included. <br>
	 * For more details see {@link isUpperLimitIncluded#upperLimitIncluded}.
	 */
	BooleanResource lowerLimitIncluded();
}
