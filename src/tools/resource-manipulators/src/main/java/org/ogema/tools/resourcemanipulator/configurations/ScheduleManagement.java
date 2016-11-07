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
