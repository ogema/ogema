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
	 * For more details see {@link #upperLimitIncluded()}.
	 */
	BooleanResource lowerLimitIncluded();
}
