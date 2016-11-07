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

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.timeseries.TimeSeries;
import org.ogema.tools.timeseries.api.FloatTimeSeries;
import org.ogema.tools.timeseries.implementations.FloatTreeTimeSeries;

/**
 * Downsampling of data points, to a minimum time interval between adjacent points. 
 * Note: only values of the same quality are aggregated, values of different quality 
 * can still appear at a higher frequency than the specified one.
 */
public class InterpolationAction extends TimeSeriesReduction {
	
	private final long minInterval;
	
	public InterpolationAction(long minInterval, ApplicationManager am) {
		super(am);
		this.minInterval = minInterval;
	}
	
	@Override
	public void apply(TimeSeries schedule, long ageThreshold) {
		long current = am.getFrameworkTime();
		long boundary;
		try {
			boundary = subtract(current, ageThreshold);
		} catch (ArithmeticException e) {
			logger.error("Arithmetic exception",e);
			return;
		}
		FloatTimeSeries fts = new FloatTreeTimeSeries();
		fts.read(schedule, Long.MIN_VALUE, boundary);
		schedule.replaceValues(Long.MIN_VALUE, boundary, fts.downsample(Long.MIN_VALUE, boundary, minInterval));
	}
	
//	@Override
//	public void apply(TimeSeries schedule, long ageThreshold) {
//		long current = am.getFrameworkTime();
//		long boundary;
//		try {
//			boundary = subtract(current, ageThreshold);
//		} catch (ArithmeticException e) {
//			logger.error("Arithmetic exception",e);
//			return;
//		}
//		InterpolationMode im  = schedule.getInterpolationMode();
//		List<SampledValue> oldValues = schedule.getValues(Long.MIN_VALUE, boundary);
//		int oldSize = oldValues.size();
//		List<SampledValue> newValues = new ArrayList<SampledValue>();
//		long lastT = Long.MIN_VALUE;
//		Quality lastQuality = Quality.GOOD;
//		List<SampledValue> currentList = new ArrayList<SampledValue>();
//		for (int i = 0;i<oldValues.size(); i++) {
//			SampledValue sv = oldValues.get(i);
//			long t0 = sv.getTimestamp();
//			Quality qual = sv.getQuality();
//			if (qual != lastQuality || t0 - lastT >= minInterval) {
//				downsample(currentList, sv, newValues, im);
//				currentList.clear();
//			}
//			currentList.add(sv);
//			lastQuality = qual;
//			lastT = t0;
//		}
//		SampledValue last = schedule.getValue(boundary); // may be null
//		downsample(currentList, last, newValues, im);
//					
//		schedule.replaceValues(Long.MIN_VALUE, boundary, newValues);
//		OgemaLogger logger = am.getLogger();
//		if (logger.isDebugEnabled())
//			logger.debug("Schedule management downsampled " + oldSize + " data points to " + schedule.getValues(Long.MIN_VALUE, current - ageThreshold).size()
//				+ " new data points");
//	}
//	
//	private void downsample(List<SampledValue> oldSubset, SampledValue endPoint, List<SampledValue> newValues, InterpolationMode mode) {
//		if (oldSubset.isEmpty()) 
//			return;
//		if (mode == InterpolationMode.NONE)
//			downsampleNaive(oldSubset, newValues);
//		else {
//			if (endPoint != null)
//				oldSubset.add(endPoint); // for the calculation of the interval we need the end point as well
//			downsample(oldSubset, newValues);
//		}
//	}
//
//	/**
//	 * Downsampling based on the integration function in memory-timeseries... taking into account the interpolation mode
//	 * @param oldSubset
//	 * @param newValues
//	 */
//	private void downsample(List<SampledValue> oldSubset, List<SampledValue> newValues) {
//		int sz = oldSubset.size()-1; 
//		long t0 = oldSubset.get(0).getTimestamp();
//		long t1 = oldSubset.get(sz).getTimestamp();
//		Quality quality = oldSubset.get(0).getQuality();
//		long delta = t1-t0;
//		int nr = (int) (delta/minInterval); 
//		
//		FloatTimeSeries fts = new FloatTreeTimeSeries();
//		fts.addValues(oldSubset);
//		for (int i=0;i<nr-1;i++) {
//			float value = fts.integrate(t0 + i*minInterval, t0 + (i+1)*minInterval) / minInterval;
//			newValues.add(new SampledValue(new FloatValue(value), t0 + i*minInterval, quality));
//		}
//		float value = fts.integrate(t0 + (nr-1)*minInterval, t1) / (t1 - t0 - (nr-1)*minInterval);
//		newValues.add(new SampledValue(new FloatValue(value), t0 + (nr-1)*minInterval, quality));
//	}
//	
//	/**
//	 * Subdivide the interval into parts of equal length, and simply take the average of all points in the
//	 * target intervals, to calculate the new values. In this case, there is no interpolation information.
//	 * @param oldSubset
//	 * @param newValues
//	 */
//	private void downsampleNaive(List<SampledValue> oldSubset, List<SampledValue> newValues) {
//		float currentValue = 0;
//		int nrCurrentElements = 0;
//		SampledValue sv = oldSubset.get(0);
//		long lastTs = sv.getTimestamp();
//		for (int i = 0;i<oldSubset.size();i++) {
//			sv = oldSubset.get(i);
//			long t = sv.getTimestamp();
//			if (t < lastTs + minInterval) { 
//				currentValue += sv.getValue().getFloatValue();
//				nrCurrentElements++;
//			}
//			else {
//				currentValue = currentValue/nrCurrentElements;
//				newValues.add(new SampledValue(new FloatValue(currentValue), lastTs, sv.getQuality()));
//				lastTs = t;
//				
//				currentValue = sv.getValue().getFloatValue();
//				nrCurrentElements = 1;
//			}
//			
//		}
//		currentValue = currentValue/nrCurrentElements;
//		newValues.add(new SampledValue(new FloatValue(currentValue), lastTs, sv.getQuality()));
//		
//	}
//	
	public long getMinInterval() {
		return minInterval;
	}
	
}
