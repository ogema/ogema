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
package org.ogema.tools.resourcemanipulator.model;

import org.ogema.core.model.ResourceList;
import org.ogema.core.model.array.TimeArrayResource;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.TimeResource;

/**
 * Data model for configuring a ScheduleSum modifier rule.
 * 
 * @author Timo Fischer, Fraunhofer IWES
 */
public interface ScheduleSumModel extends ResourceManipulatorModel {
	/**
	 * Input values. If an input value is not active (or non-existing), the input
	 * is considered as zero.
	 */
	ResourceList<Schedule> inputs();

	/**
	 * Seed resource for the output value. Real output is referenced at .program().
	 */
	//	Schedule result();
	FloatResource resultBase();

	/**
	 * Delay time between detected changes in the inputs and evaluation of the results.
	 */
	TimeResource delay();

	/**
	 * Flag defining how to handle empty sums.
	 */
	BooleanResource deactivateEmptySum();

	/**
	 * Flag defining if automatic activation/deactivation is turned on or off.
	 */
	BooleanResource activationControl();
	
	/**
	 * If true, incremental updates will be performed, starting at the latest data point
	 * in the output schedule. Otherwise, all values will be recalculated, which is potentially
	 * expensive. <br>
	 * Default value: false.
	 * @return
	 */
	BooleanResource incrementalUpdate();
	
	/**
	 * If this true, then the domain of the target schedule will be the union of the domains 
	 * of the addends, i.e. the target schedule will contain a valid value where at least one of
	 * the constituents is defined. 
	 * If false, the target domain is the intersection of the domains of the addends,
	 * i.e. the target schedule will only contain a value where all constituents are defined. <br>
	 * Default value: false.
	 * 
	 * @return
	 */
	BooleanResource ignoreGaps();
	
	/**
	 * Wait until all input schedules have a value newer than the one to be evaluated, or
	 * write immediately?
	 * @return
	 */
	BooleanResource writeImmediately();
	
	/**
	 * Internal state variable
	 * @return
	 */
	TimeArrayResource latestTimestamps();
	
}
