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
package org.ogema.tools.resource.util;

import org.ogema.core.model.schedule.AbsoluteSchedule;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.recordeddata.RecordedData;
import org.ogema.core.recordeddata.RecordedDataConfiguration;
import org.ogema.core.recordeddata.RecordedDataConfiguration.StorageType;

/**
 * This class provides convenience methods for logging configurations. It wraps 
 * the basic API methods provided by {@link RecordedData}
 * and {@link RecordedDataConfiguration}. 
 */
public class LoggingUtils {

	// no need to construct this
	private LoggingUtils() {}
	
	/**
	 * Check whether logging is enabled for the resource. This is a convenience method,
	 * the same check can be performed using only basic API methods, such as  
	 * {@link FloatResource#getHistoricalData()}, and the methods provided by {@link RecordedData}
	 * and {@link RecordedDataConfiguration}.
	 * @param resource
	 * @return
	 */
	public static boolean isLoggingEnabled(SingleValueResource resource) {
		try {
			RecordedData rd = getHistoricalData(resource);
			if (rd == null || rd.getConfiguration() == null)
				return false;
			else
				return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	/**
	 * Activate logging, or change log configuration, if logging is enabled already. This is a convenience method,
	 * logging can be enabled as well using only basic API methods, such as  
	 * {@link FloatResource#getHistoricalData()}, and the methods provided by {@link RecordedData}
	 * and {@link RecordedDataConfiguration}.
	 * @param resource
	 * @param updateInterval -1: Update on value changed, -2: Update on value updated, &gt; 0: Fixed update interval,
	 * 		interval = value in ms
	 * @throws IllegalArgumentException
	 * 		if <code>resource</code> is a {@link StringResource}
	 */
	public static void activateLogging(SingleValueResource resource, long updateInterval)
			throws IllegalArgumentException {
		RecordedData rd = getHistoricalData(resource);
		RecordedDataConfiguration rcd = new RecordedDataConfiguration();
		switch ((int) updateInterval) {
		case -1:
			rcd.setStorageType(StorageType.ON_VALUE_CHANGED);
			break;
		case -2:
			rcd.setStorageType(StorageType.ON_VALUE_UPDATE);
			break;
		default:
			if (updateInterval <= 0)
				throw new IllegalArgumentException("Logging interval must be positive");
			rcd.setStorageType(StorageType.FIXED_INTERVAL);
			rcd.setFixedInterval(updateInterval);
			break;
		}
		rd.setConfiguration(rcd);
		//write initial value
		//		if(updateInterval == -2) {
		//			res.setValue(res.getValue());
		//		}
	}

	/**
	 * A convenience method for disabling logging.
	 * @param resource
	 * @throws IllegalArgumentException
	 * 		if <code>resource</code> is a {@link StringResource}
	 */
	public static void deactivateLogging(SingleValueResource resource) throws IllegalArgumentException {
		RecordedData rd = getHistoricalData(resource);
		rd.setConfiguration(null);
	}

	/**
	 * See also {@link #getHistoricalDataSchedule(SingleValueResource)}, which provides an alternative
	 * access to log data, via a {@link Schedule}.
	 * @param resource
	 * @return
	 * @throws IllegalArgumentException
	 * 		if <code>resource</code> is a {@link StringResource}. Logging StringResources is not possible.
	 */
	public static RecordedData getHistoricalData(SingleValueResource resource) throws IllegalArgumentException {
		RecordedData rd = null;
		if (resource instanceof FloatResource)
			rd = ((FloatResource) resource).getHistoricalData();
		else if (resource instanceof IntegerResource)
			rd = ((IntegerResource) resource).getHistoricalData();
		else if (resource instanceof TimeResource)
			rd = ((TimeResource) resource).getHistoricalData();
		else if (resource instanceof BooleanResource)
			rd = ((BooleanResource) resource).getHistoricalData();
		else if (resource instanceof StringResource)
			throw new IllegalArgumentException("Logging for StringResources not possible");
		return rd;
	}

	/**
	 * Returns the <code>historicalData</code> subresource of a {@link FloatResource}, {@link IntegerResource},
	 * {@link FloatResource}, {@link BooleanResource}, or {@link TimeResource}.
	 * @param resource
	 * @return
	 * @throws IllegalArgumentException
	 * 		if <code>resource</code> is a {@link StringResource}. Logging StringResources is not possible.
	 */
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
