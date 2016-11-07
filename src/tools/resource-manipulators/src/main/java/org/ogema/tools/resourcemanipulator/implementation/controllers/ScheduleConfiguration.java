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
package org.ogema.tools.resourcemanipulator.implementation.controllers;

import java.util.NavigableMap;
import java.util.TreeMap;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.tools.resourcemanipulator.model.ScheduleManagementModel;
import org.ogema.tools.resourcemanipulator.model.schedulemgmt.ScheduleDeletionAction;
import org.ogema.tools.resourcemanipulator.model.schedulemgmt.ScheduleDownsamplingAction;
import org.ogema.tools.resourcemanipulator.model.schedulemgmt.ScheduleReductionAction;
import org.ogema.tools.resourcemanipulator.model.schedulemgmt.ScheduleStepsReductionAction;
import org.ogema.tools.resourcemanipulator.schedulemgmt.DeletionAction;
import org.ogema.tools.resourcemanipulator.schedulemgmt.InterpolationAction;
import org.ogema.tools.resourcemanipulator.schedulemgmt.StepsReductionAction;
import org.ogema.tools.resourcemanipulator.schedulemgmt.TimeSeriesReduction;

public class ScheduleConfiguration implements Controller {

	public final static long DEFAULT_UPDATE_INTERVAL = 60000 * 60 * 24; // once per day
	private static final long MINIMUM_UPDATE_INTERVAL = 60000;
	private final Schedule schedule;
	private long lastExecutionTime = -1;
	private final long executionInterval;
	private final ScheduleManagementController smc;
	private final ApplicationManager am;
	// Map<AgeThreshold, Action>
	private final NavigableMap<Long, TimeSeriesReduction> actions = new TreeMap<Long, TimeSeriesReduction>();
	
	public ScheduleConfiguration(ScheduleManagementModel item, ScheduleManagementController smc, ApplicationManager am) {
		this.schedule = item.targetResourceParent().getSubResource("schedule", Schedule.class);
		this.smc = smc;
		this.am = am;
		// FIXME we assume here that the targetResource exists... would be sensible to use patterns instead
		long updateInterval = DEFAULT_UPDATE_INTERVAL;
		if (item.updateInterval().isActive()) {
			long val = item.updateInterval().getValue();
			if (val < MINIMUM_UPDATE_INTERVAL)
				val = MINIMUM_UPDATE_INTERVAL;
			updateInterval = val;
		}
		executionInterval = updateInterval;
		for (ScheduleReductionAction act: item.actions().getAllElements()) {
			if (!act.isActive() || !act.ageThreshold().isActive())
				continue;
			TimeSeriesReduction tsr = null;
			if (act instanceof ScheduleDeletionAction)
				tsr = new DeletionAction(am);
			else if (act instanceof ScheduleDownsamplingAction &&
					((ScheduleDownsamplingAction) act).minInterval().isActive()) 
				tsr = new InterpolationAction(((ScheduleDownsamplingAction) act).minInterval().getValue(), am);
			else if (act instanceof ScheduleStepsReductionAction) 
				tsr = new StepsReductionAction(am);
			if (tsr != null) {
				addAction(act.ageThreshold().getValue(), tsr);
			}
		}
	}
	
	public void addAction(long ageThreshold, TimeSeriesReduction action) {
		actions.put(ageThreshold, action);
	}
	
	// TODO check: synchro required?
	public NavigableMap<Long, TimeSeriesReduction> getActions() {
		return new TreeMap<Long, TimeSeriesReduction>(actions);
	}
	
	public Schedule getSchedule() {
		return schedule;
	}
	
	public long getLastExecutionTime() {
		return lastExecutionTime;
	}
	
	public void setLastExecutionTime(long time) {
		this.lastExecutionTime  =time;
	}
	
	public long getExecutionInterval() {
		return executionInterval;
	}
	
	@Override
	public void start() {
		smc.addConfiguration(this);
	}

	@Override
	public void stop() {
		smc.removeConfiguration(this);
	}
}
