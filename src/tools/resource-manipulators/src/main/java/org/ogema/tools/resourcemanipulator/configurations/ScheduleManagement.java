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

import java.util.NavigableMap;

import org.ogema.core.model.schedule.Schedule;
import org.ogema.tools.resourcemanipulator.schedulemgmt.TimeSeriesReduction;

/**
 * Application tool that manages old schedule data. It can be configured 
 * to delete data points older than a specified threshold, and/or downsample
 * old values. 
 *
 * @author cnoelle, Fraunhfoer IWES
 */
public interface ScheduleManagement extends ManipulatorConfiguration {

	void manageSchedule(Schedule schedule, NavigableMap<Long,TimeSeriesReduction> actions);
	
	// Map: <ageThreshold, Action>
	void manageSchedule(Schedule schedule, long updateInterval, NavigableMap<Long,TimeSeriesReduction> actions);

	Schedule getTargetSchedule();
	
	/**
	 * Get the configured actions. The map key is the age threshold for data points 
	 * to which the action shall be applied. 
	 */
	NavigableMap<Long,TimeSeriesReduction> getActions();
	
	long getUpdateInterval();

	/**
	 * 
	 * @param interval
	 * 		update interval in ms. Note that this only takes effect after a commit
	 */
	void setUpdateInterval(long interval);

	
}
