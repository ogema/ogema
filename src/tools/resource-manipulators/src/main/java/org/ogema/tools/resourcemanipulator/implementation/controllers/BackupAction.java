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

import java.util.List;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.model.schedule.AbsoluteSchedule;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.recordeddata.RecordedData;

/*
 * Deals with historicalData schedules, which allow access to log data and explicitly set historical values.
 * Since log data is deleted periodically by the framework, we need to override it in the historical schedule 
 * by explicit data points. 
 */
public class BackupAction {
	
	public final static Long LOG_DATA_LIFETIME; // may be null, if property not set
	
	static {
		String logDataLifetimeStr = System.getProperty("org.ogema.recordeddata.slotsdb.limit_days");
		Long value = null;
		try {
			value = Long.parseLong(logDataLifetimeStr) * 24*60*60*1000;
		}  catch (NullPointerException | NumberFormatException e) {}
		LOG_DATA_LIFETIME = value;
	}
	
	public static boolean checkForHistoricalSchedule(AbsoluteSchedule schedule, ApplicationManager am) {
		AbsoluteSchedule localizedSchedule = am.getResourceAccess().getResource(schedule.getLocation());
		if (!localizedSchedule.getName().equals("historicalData"))
			return false;
		if (LOG_DATA_LIFETIME == null) {
//			throw new RuntimeException("Log data lifetime not set... cannot backup log data.");
			return true;
		}
		SingleValueResource parent = localizedSchedule.getParent();
		persistLogData(parent, am);
		return true;
	}
	
	private static void persistLogData(SingleValueResource resource, ApplicationManager am) {
		long currentTime = am.getFrameworkTime();
		RecordedData rd = getHistoricalData(resource);
		List<SampledValue> values = rd.getValues(currentTime - LOG_DATA_LIFETIME, currentTime);
		AbsoluteSchedule historicalDataSchedule = getHistoricalDataSchedule(resource).create();
		// replaces log data values by explicitly set values, which are retained after expiry of the log data
		historicalDataSchedule.addValues(values); 
		historicalDataSchedule.activate(false);
	}
	
	// duplicates a resource-utils method...
	private static RecordedData getHistoricalData(SingleValueResource resource) {
		RecordedData rd = null;
		if (resource instanceof FloatResource)
			rd = ((FloatResource) resource).getHistoricalData();
		else if (resource instanceof IntegerResource)
			rd = ((IntegerResource) resource).getHistoricalData();
		else if (resource instanceof TimeResource)
			rd = ((TimeResource) resource).getHistoricalData();
		else if (resource instanceof BooleanResource)
			rd = ((BooleanResource) resource).getHistoricalData();
		else 
			throw new IllegalArgumentException("Logging not possible for resource of type " + resource.getResourceType().getName());
		return rd;
	}
	
	// duplicates a resource-utils method...
	public static AbsoluteSchedule getHistoricalDataSchedule(SingleValueResource resource) throws IllegalArgumentException {
		AbsoluteSchedule schedule = null;
		if (resource instanceof FloatResource)
			schedule = ((FloatResource) resource).historicalData();
		else if (resource instanceof IntegerResource)
			schedule = ((IntegerResource) resource).historicalData();
		else if (resource instanceof TimeResource)
			schedule = ((TimeResource) resource).historicalData();
		else if (resource instanceof BooleanResource)
			schedule = ((BooleanResource) resource).historicalData();
		else if (resource instanceof StringResource)
			throw new IllegalArgumentException("Logging for StringResources not possible");
		return schedule;
	}
	
}
