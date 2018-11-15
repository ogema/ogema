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
package org.ogema.tools.resourcemanipulator.implementation.controllers;

import java.util.NavigableMap;
import java.util.TreeMap;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.tools.resourcemanipulator.configurations.ManipulatorConfiguration;
import org.ogema.tools.resourcemanipulator.configurations.ScheduleManagement;
import org.ogema.tools.resourcemanipulator.model.ResourceManipulatorModel;
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
	private volatile Long lastExecutionTime = null;
	private final long executionInterval;
	private final ScheduleManagementModel config; 
	private final ScheduleManagementController smc;
	private final ApplicationManager am;
	// Map<AgeThreshold, Action>
	private final NavigableMap<Long, TimeSeriesReduction> actions = new TreeMap<Long, TimeSeriesReduction>();
	
	public ScheduleConfiguration(ScheduleManagementModel item, ScheduleManagementController smc, ApplicationManager am) {
		this.config = item;
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
	
	@Override
	public Class<? extends ManipulatorConfiguration> getType() {
		return ScheduleManagement.class;
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
	
	public Long getLastExecutionTime() {
		return lastExecutionTime;
	}
	
	public void setLastExecutionTime(long time) {
		this.lastExecutionTime  =time;
	}
	
	public long getExecutionInterval() {
		return executionInterval;
	}
	
	@Override
	public ResourceManipulatorModel getConfigurationResource() {
		return config;
	}
	
	@Override
	public void start() {
		smc.addConfiguration(this);
	}

	@Override
	public void stop() {
		smc.removeConfiguration(this);
	}
	
	@Override
	public String toString() {
		return "ScheduleConfiguration for configuration " + getConfigurationResource().getName();
	}
}
