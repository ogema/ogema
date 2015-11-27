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
package org.ogema.tools.resourcemanipulator.configurations;

import java.util.Collection;
import java.util.List;
import org.ogema.core.model.schedule.Schedule;

/**
 * Automatically-evaluated sum mapping {inputs} -> sum({inputs}) = result.
 * Version for schedules, where summation is point-wise and range of the
 * result is the intersection of the input ranges. Inactive addends are
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
	 * Gets the target resource in which the result is being written.
	 */
	Schedule getTarget();

	/**
	 * Gets an unmodifiable list with the input inputs.
	 */
	List<Schedule> getAddends();
}
