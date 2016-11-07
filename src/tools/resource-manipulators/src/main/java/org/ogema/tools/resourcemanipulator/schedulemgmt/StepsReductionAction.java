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
 * For a time series with interpolation mode {@see org.ogema.core.timeseries.InterpolationMode#STEPS}
 * or {@see org.ogema.core.timeseries.InterpolationMode#NONE},
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
