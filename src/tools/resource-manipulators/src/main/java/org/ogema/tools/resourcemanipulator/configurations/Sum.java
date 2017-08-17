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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
