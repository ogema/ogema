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
package org.ogema.recordeddata.slotsdb;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.ogema.core.channelmanager.measurements.IllegalConversionException;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.recordeddata.RecordedDataConfiguration;
import org.ogema.core.recordeddata.RecordedDataConfiguration.StorageType;
import org.ogema.core.recordeddata.ReductionMode;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.recordeddata.DataRecorderException;
import org.ogema.recordeddata.RecordedDataStorage;
import org.ogema.recordeddata.slotsdb.reduction.Reduction;
import org.ogema.recordeddata.slotsdb.reduction.ReductionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SlotsDbStorage implements RecordedDataStorage {

	private RecordedDataConfiguration configuration;
	private final String id;
	private final SlotsDb recorder;

	private final static Logger logger = LoggerFactory.getLogger(SlotsDbStorage.class);

	public SlotsDbStorage(String id, RecordedDataConfiguration configuration, SlotsDb recorder) {
		this.configuration = configuration;
		this.id = id;
		this.recorder = recorder;
	}

	@Override
	public void insertValue(SampledValue value) throws DataRecorderException {
		try {
			if (configuration.getStorageType() == StorageType.FIXED_INTERVAL) {
				recorder.proxy.appendValue(id, value.getValue().getDoubleValue(), value.getTimestamp(), (byte) value
						.getQuality().getQuality(), configuration.getFixedInterval());
			}
			else {

				recorder.proxy.appendValue(id, value.getValue().getDoubleValue(), value.getTimestamp(), (byte) value
						.getQuality().getQuality(), -1);
			}

		} catch (IOException e) {
			logger.error("", e);
		} catch (IllegalConversionException e) {
			logger.error("", e);
		}
	}

	@Override
	public void insertValues(List<SampledValue> values) throws DataRecorderException {
		try {
			for (SampledValue value : values) {
				recorder.proxy.appendValue(id, value.getValue().getDoubleValue(), value.getTimestamp(), (byte) value
						.getQuality().getQuality(), configuration.getFixedInterval());
			}
		} catch (IOException e) {
			logger.error("", e);
		} catch (IllegalConversionException e) {
			logger.error("", e);
		}
	}

	@Override
	public List<SampledValue> getValues(long startTime) {
		List<SampledValue> records = null;
		try {
			records = recorder.proxy.read(id, startTime, System.currentTimeMillis());
		} catch (IOException e) {
			logger.error("", e);
		}
		return records;
	}

	@Override
	public List<SampledValue> getValues(long startTime, long endTime) {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss.SSS");

		List<SampledValue> records = null;
		try {
			records = recorder.proxy.read(id, startTime, endTime - 1);
		} catch (IOException e) {
			logger.error("", e);
		}
		return records;
	}

	@Override
	public SampledValue getValue(long timestamp) {
		try {
			return recorder.proxy.read(id, timestamp);
		} catch (IOException e) {
			logger.error("", e);
		}
		return null;
	}

	@Override
	public void setConfiguration(RecordedDataConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	public RecordedDataConfiguration getConfiguration() {
		return configuration;
	}

	@Override
	public List<SampledValue> getValues(long startTime, long endTime, long intervalSize, ReductionMode mode) {

		// TODO could cause crash? extremely long requested time period of very small sampled values
		List<SampledValue> returnValues = new ArrayList<SampledValue>();

		// last timestamp is exclusive and therefore not part of the request
		endTime = endTime - 1;

		if (validateArguments(startTime, endTime, intervalSize)) {

			// Issues to consider:
			// a) calling the for each subinterval will slow down the reduction (many file accesses)
			// b) calling the read for the whole requested time period might be problematic - especially when requested time
			// period is large and the log interval was very small (large number of values)
			// Compromise: When the requested time period covers multiple days and therefore multiple log files, then a
			// separate read data processing is performed for each file.
			List<SampledValue> loggedValuesRaw = getLoggedValues(startTime, endTime);
			List<SampledValue> loggedValues = removeQualityBad(loggedValuesRaw);

			if (loggedValues.isEmpty()) {
				// return an empty list since there are no logged values, so it doesn't make sense to aggregate anything
				return returnValues;
			}

			if (mode.equals(ReductionMode.NONE)) {
				return loggedValues;
			}

			List<Interval> intervals = generateIntervals(startTime, endTime, intervalSize);
			returnValues = generateReducedData(intervals, loggedValues, mode);

		}

		return returnValues;
	}

	/**
	 * Generates equidistant intervals starting from periodStart till periodEnd. The last interval might have a
	 * different length than intervalSize.
	 * 
	 * ASSUMPTION: Arguments are valid (see validateArguments method)
	 *
	 * @return List of intervals which cover the entire period
	 */
	private List<Interval> generateIntervals(long periodStart, long periodEnd, long intervalSize) {

		List<Interval> intervals = new ArrayList<Interval>();

		long start = periodStart;
		long end;
		do {
			end = start + intervalSize - 1;
			if (end > periodEnd) {
				end = periodEnd;
			}
			intervals.add(new Interval(start, end));
			start = end + 1;
		} while (end != periodEnd);

		return intervals;
	}

	private List<SampledValue> generateReducedData(List<Interval> intervals, List<SampledValue> loggedValues,
			ReductionMode mode) {

		List<SampledValue> returnValues = new ArrayList<SampledValue>();
		List<SampledValue> reducedValues;

		ReductionFactory reductionFactory = new ReductionFactory();
		Reduction reduction = reductionFactory.getReduction(mode);

		int index = 0; // used to move forwards in the loggedValues list
		int maxIndex = loggedValues.size() - 1;
		SampledValue loggedValue = loggedValues.get(index);
		long timestamp = loggedValue.getTimestamp();

		Iterator<Interval> it = intervals.iterator();
		Interval interval;

		while (it.hasNext()) {

			interval = it.next();

			while (timestamp >= interval.getStart() && timestamp <= interval.getEnd()) {

				interval.add(loggedValue);

				if (index < maxIndex) {
					index++;
					loggedValue = loggedValues.get(index);
					timestamp = loggedValue.getTimestamp();
				}
				else {
					// complete loggedValues list is processed
					break;
				}
			}

			reducedValues = reduction.performReduction(interval.getValues(), interval.getStart());
			returnValues.addAll(reducedValues);

		}

		//debug_printIntervals(intervals);

		return returnValues;
	}

	private void debug_printIntervals(List<Interval> intervals) {

		Iterator<Interval> it2 = intervals.iterator();
		Interval interval2;

		int i = 1;
		while (it2.hasNext()) {
			interval2 = it2.next();
			System.out.println(i + ": " + interval2.getValues().toString());
			i++;
		}
	}

	private boolean validateArguments(long startTime, long endTime, long interval) {

		boolean result = false;

		if (startTime > endTime) {
			logger.warn("Invalid parameters: Start timestamp musst be smaller than end timestamp");
		}
		else if (interval < 0) {
			logger.warn("Invalid arguments: interval musst be > 0");
		}
		else {
			result = true;
		}

		return result;
	}

	/**
	 * 
	 * @param startTime
	 * @param endTime
	 * @param intervalSize
	 * @return List with average values on success, otherwise empty list.
	 */
	private List<SampledValue> getLoggedValues(long startTime, long endTime) {
		try {
			List<SampledValue> loggedValues = recorder.proxy.read(id, startTime, endTime);
			return loggedValues;
		} catch (IOException e) {
			e.printStackTrace();
			return new ArrayList<SampledValue>();
		}
	}

	private List<SampledValue> removeQualityBad(List<SampledValue> toReduce) {
		List<SampledValue> result = new ArrayList<SampledValue>();
		for (SampledValue val : toReduce) {
			if (val.getQuality() != Quality.BAD) {
				result.add(val);
			}
		}
		return result;
	}

	@Override
	public void update(RecordedDataConfiguration configuration) throws DataRecorderException {
		this.configuration = configuration;

		HashMap<String, RecordedDataConfiguration> persistentConfigurationMap = getPersistenConfigurationMap();
		persistentConfigurationMap.put(this.id, configuration);
		setPersistenConfigurationMap(persistentConfigurationMap);
	}

	@Override
	public SampledValue getNextValue(long time) {
		try {
			return recorder.proxy.readNextValue(id, time);
		} catch (IOException e) {
			logger.error("", e);
			return null;
		}

	}

	@Override
	public Long getTimeOfLatestEntry() {
		// TODO
		return null;
	}

	@Override
	public InterpolationMode getInterpolationMode() {
		return InterpolationMode.NONE;
	}

	private void setPersistenConfigurationMap(HashMap<String, RecordedDataConfiguration> persistentConfigurationMap) {
		ObjectOutputStream oosConfig = null;
		try {
			oosConfig = new ObjectOutputStream(new FileOutputStream(SlotsDb.CONFIGURATION_PATH));
			oosConfig.writeObject(persistentConfigurationMap);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (oosConfig != null) {
				try {
					oosConfig.close();
				} catch (IOException e) {
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public HashMap<String, RecordedDataConfiguration> getPersistenConfigurationMap() {
		HashMap<String, RecordedDataConfiguration> persistentConfigurationMap = null;
		ObjectInputStream ois = null;

		try {
			ois = new ObjectInputStream(new FileInputStream(SlotsDb.CONFIGURATION_PATH));
			persistentConfigurationMap = (HashMap<String, RecordedDataConfiguration>) ois.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (ois != null) {
				try {
					ois.close();
				} catch (IOException e) {
				}
			}
		}
		return persistentConfigurationMap;
	}
}

class Interval {

	List<SampledValue> values = new ArrayList<SampledValue>();

	private long start;
	private long end;

	public Interval(long start, long end) {
		this.start = start;
		this.end = end;
	}

	public long getEnd() {
		return end;
	}

	public long getStart() {
		return start;
	}

	public void add(SampledValue value) {
		values.add(value);
	}

	public List<SampledValue> getValues() {
		return values;
	}
}
