package org.ogema.tools.timeseriesimport.impl;

import java.util.List;

import org.ogema.core.channelmanager.measurements.SampledValue;

interface CacheAccess {

	boolean isLoaded();
	List<SampledValue> getValues(long startTime, long endTime);
	SampledValue getFirstValue();
	
}
