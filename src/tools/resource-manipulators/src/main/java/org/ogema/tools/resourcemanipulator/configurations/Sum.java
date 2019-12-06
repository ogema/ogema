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
package org.ogema.tools.resourcemanipulator.configurations;

import java.util.Collection;
import java.util.List;

import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.SingleValueResource;

/**
 * Automatically-evaluated sum mapping {inputs} -&gt; sum({inputs}) = result.
 * Version for single values.
 * Inactive addends are ignored in the sum.
 * 
 * Note: Currently, only float-valued schedules are supported.
 * 
 * TODO later versions of this may want to support exclusive write accesses.
 * 
 * @author Marco Postigo Perez, Fraunhofer IWES
 */
public interface Sum extends ManipulatorConfiguration {

	/**
	 * Sets the list of addends and the target resource where the result
	 * is written to.
	 * @param addends List of schedules to add up.
	 * @param sum target schedule that the result is written to.
	 */
	void setAddends(Collection<? extends SingleValueResource> addends, SingleValueResource sum);

	/**
	 * Sets the list of addends and the target resource where the result
	 * is written to.
	 * @param addends List of schedules to add up.
	 * @param sum target schedule that the result is written to.
	 * @param factors a list of factors to multiply the addends with; may be null, which is equivalent to all 1s
	 * @param offsets a list of offsets to be added to the addends; may be null, which is equivalent to all 0s
	 * @throws IllegalArgumentException if the size of the factors or offsets parameters does not 
	 *  	match the size of the addends parameters (unless factors or offsets is null, which is allowed)
	 * 
	 */
	void setAddends(List<? extends SingleValueResource> addends, List<Float> factors, List<Float> offsets, SingleValueResource sum);
	
	/**
	 * Sets a delay time between changes in the inputs and the evaluation of the output.
	 * this can be useful in situation where one expects multiple of the inputs
	 * to change at the same time. The default delay of a newly-created rule is zero.
	 * 
	 * @param delayTime time to wait after first detected input change in ms.
	 */
	void setDelay(long delayTime);

	/**
	 * Gets the delay time configured for this.
	 * @return computation delay time
	 */
	long getDelay();

	/**
	 * Sets the behavior with respect to empty sums. If disabled then the target
	 * will become inactive. 
	 * @param emptySumDisables New treatment for empty sums, i.e. sums in which
	 * no active addend exists.
	 */
	void setDisableEmptySum(boolean emptySumDisables);

	/**
	 * @return configured handling of empty sums. 
	 * @see #setDisableEmptySum(boolean) 
	 */
	boolean getDisableEmptySum();

	/**
	 * Gets the target resource in which the result is being written.
	 * @return the output resource
	 */
	Resource getTarget();

	/**
	 * Gets an unmodifiable list with the inputs.
	 * @return list of input resources
	 */
	List<SingleValueResource> getAddends();
}
