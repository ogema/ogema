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
