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
