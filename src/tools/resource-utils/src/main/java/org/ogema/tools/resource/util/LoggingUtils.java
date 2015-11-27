package org.ogema.tools.resource.util;

import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.recordeddata.RecordedData;
import org.ogema.core.recordeddata.RecordedDataConfiguration;
import org.ogema.core.recordeddata.RecordedDataConfiguration.StorageType;

public class LoggingUtils {

	public static boolean isLoggingEnabled(SingleValueResource resource) {
		try {
			RecordedData rd = getHistoricalData(resource);
			if (rd.getConfiguration() == null)
				return false;
			else
				return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	/**
	 * Activate logging, or change log configuration, if logging is enabled already
	 * @param resource
	 * @param updateInterval -1: Update on value changed, -2:Update on value updated
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
	 * @param resource
	 * @throws IllegalArgumentException
	 * 		if resource is a StringResource
	 */
	public static void deactivateLogging(SingleValueResource resource) throws IllegalArgumentException {
		RecordedData rd = getHistoricalData(resource);
		rd.setConfiguration(null);
	}

	/**
	 * @param resource
	 * @return
	 * @throws IllegalArgumentException
	 * 		if <code>res</code> is a {@link StringResource}. Logging StringResources is not possible.
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

}
