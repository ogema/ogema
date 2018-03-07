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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.application.Timer;
import org.ogema.core.application.TimerListener;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.schedule.AbsoluteSchedule;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.tools.resourcemanipulator.schedulemgmt.TimeSeriesReduction;

// FIXME read configuration from resource on every execution, so that 
// changes in the config resource are respected
public class ScheduleManagementController implements TimerListener {

	private final Timer timer;
	private final ApplicationManager am;
	private final OgemaLogger logger;
	// if two execution intervals differ only by this amount of milliseconds,
	// they may be executed jointly
	private final static long TOLERANCE_INTERVAL = 60000; 
	// Map<Schedule path, config>
	private final Map<String, ScheduleConfiguration> managedSchedules = new HashMap<String, ScheduleConfiguration>();
	
	public ScheduleManagementController(ApplicationManager am) {
		this.am= am;
		this.logger  =am.getLogger();
		this.timer = am.createTimer(10000,this); 
		timer.stop();
	}
	
	public void close() {
		timer.stop();
		managedSchedules.clear();
	}
	
	public void addConfiguration(ScheduleConfiguration config) {
		String id = config.getSchedule().getPath();
		ScheduleConfiguration old = managedSchedules.put(id, config);
//		if (old != null) // TODO do something?
		timerElapsed(timer); // execute once and configure timer
		logger.debug("Adding new schedule to managed configurations {}",config.getSchedule());
	}
	
	public void removeConfiguration(ScheduleConfiguration item) {  
		String id = item.getSchedule().getPath();
		managedSchedules.remove(id); 
		if (managedSchedules.isEmpty())
			timer.stop();
		logger.debug("Removing schedule from managed configurations {}", item.getSchedule());
	}
	
	// also does the rescheduling
	@Override
	public void timerElapsed(Timer timer) {
		timer.stop();
		long nextExecTime = Long.MAX_VALUE;
		long current = am.getFrameworkTime();
		List<ScheduleConfiguration> executables = new ArrayList<ScheduleConfiguration>();
		for (ScheduleConfiguration cfg: managedSchedules.values()) {
			long itv = cfg.getExecutionInterval();
			final Long lastExec = cfg.getLastExecutionTime();
			long next = lastExec == null ? current : lastExec + itv;
			if (next <= current + TOLERANCE_INTERVAL) {
				executables.add(cfg);
				next = current + itv;
			}
			if (next < nextExecTime)
				nextExecTime = next;
		}
		for (ScheduleConfiguration cfg : executables) {
			execute(cfg);
			cfg.setLastExecutionTime(current);
		}
		if (nextExecTime < Long.MAX_VALUE) {
			timer.setTimingInterval(nextExecTime - current);
			timer.resume();
			logger.debug("Reduction timer set to interval " + timer.getTimingInterval() + " ms");
		}
		else 
			logger.debug("Reduction timer cancelled.");
	}

	private void execute(ScheduleConfiguration cfg) {
		Schedule schedule = cfg.getSchedule();
		boolean historical = false;
		if (schedule instanceof AbsoluteSchedule) {
			historical = BackupAction.checkForHistoricalSchedule((AbsoluteSchedule) schedule, am);
			if (historical && BackupAction.LOG_DATA_LIFETIME == null) {
				logger.warn("Cannot manage historical data schedule {}, since log data lifetime is not set",schedule);
				return;
			}
		}
		try {
			for (Map.Entry<Long, TimeSeriesReduction> entry : cfg.getActions().entrySet()) {
				long threshold = entry.getKey();
				if (historical && threshold < BackupAction.LOG_DATA_LIFETIME) {
					logger.debug("Reduction threshold " + threshold +" smaller than log data lifetime "
							+ BackupAction.LOG_DATA_LIFETIME + "; setting threshold equal to the lifetime");
					threshold = BackupAction.LOG_DATA_LIFETIME;
				}
				TimeSeriesReduction red = entry.getValue();
				logger.debug("Executing schedule size reduction for {} of type {}, with threshold {}", schedule, red.getClass().getSimpleName(), threshold);
				red.apply(schedule, threshold);
			}
		} catch (Exception e) {
			logger.error("Error executing schedule reduction for " + schedule,e);
		}
	}
	
	
	
}
