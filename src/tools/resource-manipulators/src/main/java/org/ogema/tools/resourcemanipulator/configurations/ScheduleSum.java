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
import org.ogema.core.model.schedule.Schedule;

/**
 * Automatically-evaluated sum mapping {inputs} -&gt; sum({inputs}) = result.
 * Version for schedules, where summation is point-wise and domain of the
 * result is the intersection of the input domains (unless {@link #setIgnoreGaps(boolean)}),
 * is set to true, in which case it is the union of the input domains). Inactive addends are
 * ignored in the sum.
 * 
 * Note: Currently, only float-valued schedules are supported.
 * 
 * TODO later versions of this may want to support exclusive write accesses.
 * 
 * @author Timo Fischer, Fraunhofer IWES
 */
public interface ScheduleSum extends ManipulatorConfiguration {

	/**
	 * Sets the list of addends and the target resource where the result
	 * is written to.
	 * @param addends List of schedules to add up.
	 * @param sum target schedule that the result is written to.
	 */
	void setAddends(Collection<Schedule> addends, Schedule sum);

	/**
	 * Sets a delay time between changes in the inputs and the evaluation of the output.
	 * this can be useful in situation where one expects multiple of the inputs schedules
	 * to change at the same time. The default delay of a newly-created rule is zero.
	 * 
	 * @param delayTime time to wait after first detected input change in ms.
	 */
	void setDelay(long delayTime);

	/**
	 * Gets the delay time configured for this.
	 */
	long getDelay();

	/**
	 * Sets the behavior with respect to empty sums, which can either cause an
	 * empty schedule being created (default) or the target to become inactive. 
	 * @param emptySumDisables New treatment for empty sums, i.e. sums in which
	 * no active addend exists.
	 * @deprecated Definition of this was inconsistent, since inactive resources would not be re-activated. Use {@link #setActivationControl(boolean)}, instead.
	 */
	@Deprecated
	void setDisableEmptySum(boolean emptySumDisables);

	/**
	 * @return configured handling of empty sums. 
	 * @see #setDisableEmptySum(boolean) 
	 * @deprecated Definition of DisableEmtpySum was incomplete. Use the setting defined by {@link #setActivationControl(boolean)} and {@link #getActivationControl()}, instead.
	 */
	@Deprecated
	boolean getDisableEmptySum();

	/**
	 * Sets the behavior of the manipulator rule with respect to activating
	 * and de-activating the result schedule. If set to true, the
	 * rule will activate the result schedule if one or more valid addends
	 * went into the calculation. If the sum was empty, the result is set
	 * inactive. If this option is set to false (default), the rule will leave the
	 * activation status of the target resource unchanged.
	 * @param controlSetting New setting for result activation/de-activation.
	 */
	void setActivationControl(boolean controlSetting);

	/**
	 * Gets the current setting for automatic activation/de-activation of the
	 * result schedule. See {@link #setActivationControl(boolean)}
	 * @return Setting that is currently active.
	 */
	boolean getActivationControl();
	
	/**
	 * 
	 * If this set to true, then the domain of the target schedule will be the union of the domains 
	 * of the addends, i.e. the target schedule will contain a valid value whereever at least one of
	 * the constituents is defined. 
	 * If false, the target domain is the intersection of the domains of the addends,
	 * i.e. the target schedule will only contain a value where all constituents are defined. <br>
	 * Default value: false.
	 * 
	 * @param ignoreGaps
	 */
	void setIgnoreGaps(boolean ignoreGaps);
	
	/**
	 * @see #setIgnoreGaps(boolean)
	 * @return
	 */
	boolean isIgnoreGaps();
	
	/**
	 * If true, a new value is only written once all non-empty input schedules have
	 * a data point newer than the timestamp evaluated. If set to false, new values
	 * in a single schedule will lead to a new value in the sum schedule immediately (resp. after the specified delay), 
	 * which implies that the calculated value can change later on, when another input schedule value becomes available 
	 * (even if {@link #setIncrementalUpdate(boolean)} is true). 
	 * Default: true.
	 * 
	 * @param waitForSchedules
	 */
	void setWaitForSchedules(boolean waitForSchedules);
	
	/**
	 * @see #setWaitForSchedules(boolean)
	 * @return
	 */
	boolean isWaitForSchedules();
	
	/**
	 * If set to true, incremental updates will be performed, starting at the latest data point
	 * in the output schedule. Otherwise, all values will be recalculated whenever one of the input schedules changes, 
	 * which is potentially expensive. <br>
	 * If schedule values can still change after their creation, this should typically not be set to true, since
	 * once the sum has been calculated it will not be corrected later on.<br> 
	 * Default value: false.
	 * 
	 * @param incremental
	 */
	void setIncrementalUpdate(boolean incremental);
	
	/**
	 * @see #setIncrementalUpdate(boolean)
	 * @return
	 */
	boolean isIncrementalUpdateEnabled();
	
	/**
	 * Gets the target resource in which the result is being written.
	 */
	Schedule getTarget();

	/**
	 * Gets an unmodifiable list with the input inputs.
	 */
	List<Schedule> getAddends();
}
