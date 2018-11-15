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
package org.ogema.tools.resourcemanipulator.implementation;

import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.tools.resourcemanipulator.ResourceManipulatorImpl;
import org.ogema.tools.resourcemanipulator.configurations.ScheduleManagement;
import org.ogema.tools.resourcemanipulator.implementation.controllers.ScheduleConfiguration;
import org.ogema.tools.resourcemanipulator.model.ScheduleManagementModel;
import org.ogema.tools.resourcemanipulator.model.schedulemgmt.ScheduleDeletionAction;
import org.ogema.tools.resourcemanipulator.model.schedulemgmt.ScheduleDownsamplingAction;
import org.ogema.tools.resourcemanipulator.model.schedulemgmt.ScheduleReductionAction;
import org.ogema.tools.resourcemanipulator.model.schedulemgmt.ScheduleStepsReductionAction;
import org.ogema.tools.resourcemanipulator.schedulemgmt.DeletionAction;
import org.ogema.tools.resourcemanipulator.schedulemgmt.InterpolationAction;
import org.ogema.tools.resourcemanipulator.schedulemgmt.StepsReductionAction;
import org.ogema.tools.resourcemanipulator.schedulemgmt.TimeSeriesReduction;

public class ScheduleManagementImpl implements ScheduleManagement {
	
	private static final String ACTION_RES_PREFIX = "actionResource";
	private final ResourceManipulatorImpl base;
	private Schedule targetResource;
	private long updateInterval;
	private final ApplicationManager am;
	// Map<age threshold, action>
	private NavigableMap<Long,TimeSeriesReduction> actions;
//	private AccessPriority priority;
//	private boolean exclusiveAccessRequired;

	// Configuration this is connected to (null if not connected)
	private ScheduleManagementModel config;

	public ScheduleManagementImpl(ResourceManipulatorImpl base, ScheduleManagementModel configResource, ApplicationManager am) {
		this.base = base;
		this.am = am;
		this.actions = getActionConfigsFromResources(configResource.actions().getAllElements()); 
		String loc = configResource.targetResourceParent().getSubResource("schedule", Schedule.class).getLocation();
		this.targetResource = am.getResourceAccess().getResource(loc);
		this.config = configResource;
		this.updateInterval = configResource.updateInterval().getValue();
		// must not do this -> more than one impl object can be created per managed resource
		// well... actually this doesn't do anything
//		if (configResource.updateInterval().isActive()) 
//			manageSchedule(sched, configResource.updateInterval().getValue(), actions); 
//		else 
//			manageSchedule(sched, actions);  
	}

	private NavigableMap<Long,TimeSeriesReduction> getActionConfigsFromResources(List<ScheduleReductionAction> actionRes) {
		NavigableMap<Long,TimeSeriesReduction> list = new TreeMap<>();
		for (ScheduleReductionAction sra: actionRes) {
			TimeSeriesReduction tsr = getActionConfigFromResource(sra);
			if (tsr == null)
				continue;
			list.put(sra.ageThreshold().getValue(), tsr);
		}
		return list;
	}
	
	private TimeSeriesReduction getActionConfigFromResource(ScheduleReductionAction actionRes) {
		TimeSeriesReduction tsr = null;
		if (actionRes instanceof ScheduleDeletionAction) {
			tsr = new DeletionAction(am);
		}
		else if (actionRes instanceof ScheduleDownsamplingAction) {
			long minInterval = ((ScheduleDownsamplingAction) actionRes).minInterval().getValue();
			tsr = new InterpolationAction(minInterval ,am);
		}
		else if (actionRes instanceof ScheduleStepsReductionAction) {
			tsr = new StepsReductionAction(am);
		}
		return tsr;
	}
	
	private ScheduleReductionAction getResourceFromConfig(TimeSeriesReduction config, long ageThreshold) {
		ScheduleReductionAction resource = null;
		if (config instanceof DeletionAction) {
			resource = this.config.actions().getSubResource(getNextActionName(), ScheduleDeletionAction.class).create();
		}
		else if (config instanceof InterpolationAction) {
			resource = this.config.actions().getSubResource(getNextActionName(), ScheduleDownsamplingAction.class).create();
			((ScheduleDownsamplingAction) resource).minInterval().<TimeResource> create().setValue(((InterpolationAction) config).getMinInterval());
		}
		else if (config instanceof StepsReductionAction) {
			resource = this.config.actions().getSubResource(getNextActionName(), ScheduleStepsReductionAction.class).create();
		}
		else 
			throw new IllegalArgumentException("TimeSeriesReduction must be either a deletion action or a interpolation action; got " + config.getClass().getName() );
		if (resource != null) {
			resource.ageThreshold().<TimeResource> create().setValue(ageThreshold);
		}
		return resource;
	}
	
	private String getNextActionName() {
		if (config.actions() == null) return ACTION_RES_PREFIX + "_0";
		int i = 1;
		String newName = ACTION_RES_PREFIX + "_0";
		Resource res = config.actions().getSubResource(newName); 
		while (res != null) {
			newName  = ACTION_RES_PREFIX + "_" + i++;
			res = config.actions().getSubResource(newName); 
		}
		return newName;
	}
	
	
	public ScheduleManagementImpl(ResourceManipulatorImpl base, ApplicationManager am) {
		this.base = base;
		this.am = am;
		this.targetResource = null;
		this.updateInterval = ScheduleConfiguration.DEFAULT_UPDATE_INTERVAL;
		this.config = null;
		this.actions = null;
	}
	
	@Override
	public boolean commit() {
		if (targetResource == null) {
			return false;
		}
		// no need to insist on FloatResource
//		if (targetResource.getParent() == null || !(targetResource.getParent() instanceof FloatResource)) {
//			throw new IllegalArgumentException("Schedule management only deals with float schedules"); // FIXME 
//		}
		if (targetResource.getParent() == null) {
			throw new IllegalArgumentException("Schedule missing a parent resource " + targetResource);  
		}
		// delete the old configuration if it exsited.
		if (config != null) { // FIXME needn't we check here that there is no other config for the target resource?
			config.delete();
		}
		config = base.createResource(ScheduleManagementModel.class);
		config.targetResourceParent().create();
		config.targetResourceParent().addDecorator("schedule",targetResource);
		config.updateInterval().create();
		config.updateInterval().setValue(updateInterval);
		config.actions().create();
		for (Map.Entry<Long, TimeSeriesReduction> action: actions.entrySet()) {
			getResourceFromConfig(action.getValue(), action.getKey());
		}
        @SuppressWarnings("deprecation")
		org.ogema.core.resourcemanager.Transaction tr = am.getResourceAccess().createTransaction();
		tr.addResource(config);
		tr.addTree(config, false);
		tr.activate();
//		config.activate(true); // does not activate all subresource in a single transaction?
		return true;
	}

	@Override
	public void remove() {
		if (config != null && config.exists()) {
			config.delete();
		}
	}

	@Override
	public void deactivate() {
		if (config != null)
			config.deactivate(true);
	}

	@Override
	public void activate() {
		if (config != null)
			config.activate(true);
	}

	@Override
	public void manageSchedule(Schedule schedule, NavigableMap<Long,TimeSeriesReduction> actions) {
		manageSchedule(schedule, ScheduleConfiguration.DEFAULT_UPDATE_INTERVAL, actions);
	}

	@Override
	public void manageSchedule(Schedule schedule, long updateInterval, NavigableMap<Long, TimeSeriesReduction> actions) {
		this.targetResource = schedule;
		this.updateInterval = updateInterval;
		this.actions = new TreeMap<>(actions);
	}

	@Override
	public Schedule getTargetSchedule() {
		return targetResource;
	}

	@Override
	public NavigableMap<Long, TimeSeriesReduction> getActions() {
		return new TreeMap<>(actions);
	}
	
	@Override
	public long getUpdateInterval() {
		return updateInterval;
	}

	@Override
	public void setUpdateInterval(long interval) {
		this.updateInterval = interval;
	}


}
