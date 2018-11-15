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
package org.ogema.tools.resourcemanipulator.schedulemgmt;

import java.util.Iterator;
import java.util.List;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.channelmanager.measurements.IllegalConversionException;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.model.Resource;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.core.timeseries.TimeSeries;

/**
 * For a time series with interpolation mode {@link org.ogema.core.timeseries.InterpolationMode#STEPS}
 * or {@link org.ogema.core.timeseries.InterpolationMode#NONE},
 * delete all data points whose value and quality equal the previous ones. 
 */
public class StepsReductionAction extends TimeSeriesReduction {

	public StepsReductionAction(ApplicationManager am) {
		super(am);
	}
	
	@Override
	public void apply(TimeSeries schedule, long ageThreshold) {
		InterpolationMode im = schedule.getInterpolationMode();
		if (im != InterpolationMode.STEPS && im != InterpolationMode.NONE) {
			logger.warn("Trying to apply STEPS reduction to a schedule with a different interpolation mode "
				+ "{}; interpolation mode {}", schedule, im);
			return;
		}
		if (logger.isDebugEnabled())
			logger.debug("Schedule management removing equal value steps in schedule {}", schedule);
		long current = am.getFrameworkTime();
		long boundary;
		try {
			boundary = subtract(current, ageThreshold);
		} catch (ArithmeticException e) {
			logger.error("Arithmetic exception",e);
			return;
		}
		List<SampledValue> values = schedule.getValues(Long.MIN_VALUE, boundary);
		if (values.isEmpty()) 
			return;
		
		if (schedule instanceof Schedule) {
			Resource res = (am.getResourceAccess().getResource(((Schedule) schedule).getLocation())).getParent();
			if (res instanceof FloatResource) {
				logger.error("Cannot perform steps reduction on Float Schedule {}", schedule);
				return;
			}
		}
		try {
			Iterator<SampledValue> it = values.iterator();
			SampledValue start = it.next();
			int last = start.getValue().getIntegerValue();
			Quality lastQual = start.getQuality();
			while(it.hasNext()) {
				SampledValue sv = it.next();
				int val = sv.getValue().getIntegerValue();
				Quality qual = sv.getQuality();
				if (val == last && qual == lastQual) 
					it.remove();
				else {
					last = val;
					lastQual = qual;
				}
			}
		} catch (IllegalConversionException e) {
			logger.error("",e);
			return;
		}
		schedule.replaceValues(Long.MIN_VALUE, boundary, values);
	}
	
}
