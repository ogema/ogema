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
package org.ogema.tools.timeseries.api;

import org.ogema.core.channelmanager.measurements.SampledValue;

/**
 * An operation V x V -&gt; V over SampledValues V that is linear in both arguments. Examples
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
