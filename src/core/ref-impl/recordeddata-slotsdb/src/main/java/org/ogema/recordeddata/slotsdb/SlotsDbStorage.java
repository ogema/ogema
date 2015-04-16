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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ogema.core.channelmanager.measurements.DoubleValue;
import org.ogema.core.channelmanager.measurements.IllegalConversionException;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.recordeddata.RecordedDataConfiguration;
import org.ogema.core.recordeddata.RecordedDataConfiguration.StorageType;
import org.ogema.core.recordeddata.ReductionMode;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.recordeddata.DataRecorderException;
import org.ogema.recordeddata.RecordedDataStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SlotsDbStorage implements RecordedDataStorage {

	private RecordedDataConfiguration configuration;
	private final String id;
	private final SlotsDb recorder;

	private final Logger logger = LoggerFactory.getLogger(getClass());

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
	public List<SampledValue> getValues(long startTime, long endTime, long interval, ReductionMode mode) {
		if (startTime > endTime - 1) { // endTime-1 --> endTime is exclusive ...
			// FIXME log warning? throw exception? or only return empty list?!
			return Collections.emptyList();
		}

		List<SampledValue> reducedInterval = new ArrayList<SampledValue>();
		// FIXME
		// Feith: gefährlich. Was passiert, wenn requestInterval nicht exakt
		// mit gespeichertem Intervall übereinstimmt?:
		// a) gespeichert wurde mit TS = 12:01 Uhr + n* 10 min
		// abgefragt wird mit startTime=12:00 endTime=13:00 ==> kein
		// Treffer?!
		// b) gespeichert wurde mit TS = 12:00 Uhr + n* 10 min
		// abgefragt wird mit startTime=12:00, endTime=13:00,
		// requestInterval=5 min ==> halbe Anzahl erwarteter
		// Treffer?!
		// c) gespeichert wurde mit TS = 12:00 Uhr + variable Rate
		// abgefragt wird mit startTime=12:00, endTime=13:00,
		// requestInterval=??? min ==> keine exakten Treffer?!
		// FIXME
		// FEITH: Sonstiges:
		// d) Wer hat geprüft, ob ((endTime - startTime) / requestInterval)
		// ohne Rest aufgeht?
		// e) Wer hat geprüft, ob endTime > startTime ist?

		// without ReductionMode ==> get the plain RecordedData values
		// SampledValues
		if (mode == ReductionMode.NONE) {
			// this method subtracts one from endtime to get it exclusive ...
			return getValues(startTime, endTime);
		}

		// with active ReductionMode
		int nmbOfIntervals = (int) Math.ceil((endTime - startTime) / (double) interval);

		long subIntervalStart = startTime;
		long subIntervalEnd = subIntervalStart + interval - 1;
		for (int i = 0; i < nmbOfIntervals; i++) {
			if (subIntervalEnd > endTime - 1) {
				subIntervalEnd = endTime - 1;
			}

			List<SampledValue> toReduce;
			try {
				// start and end are inclusive in this read method ...
				toReduce = recorder.proxy.read(id, subIntervalStart, subIntervalEnd);
			} catch (IOException e) {
				logger.error("Error while reading time series data from db storage. " + "ID: " + id + ", start time: "
						+ startTime + ", end time: " + endTime, e);
				// add quality bad value if we can't reduce an interval for some reason:
				reducedInterval.add(new SampledValue(new DoubleValue(0), subIntervalStart, Quality.BAD));
				continue;
			}

			// remove all entries with bad quality and get mean/max/min ...
			Quality quality;
			List<SampledValue> cleanedUpList = removeQualityBad(toReduce);
			if (cleanedUpList.isEmpty()) {
				// we have an empty list or only entries with bad quality ...
				// get the mean / max / min with Quality.BAD then
				quality = Quality.BAD;
			}
			else {
				// removed all entries with quality bad
				toReduce = cleanedUpList;
				quality = Quality.GOOD;
			}

			switch (mode) {
			case AVERAGE:
				reducedInterval.add(getMeanValue(subIntervalStart, toReduce, quality));
				break;
			case MAXIMUM_VALUE:
				reducedInterval.add(getMaximumValue(subIntervalStart, toReduce, quality));
				break;
			case MINIMUM_VALUE:
				reducedInterval.add(getMinimumValue(subIntervalStart, toReduce, quality));
				break;
			case MIN_MAX_VALUE:
				reducedInterval.add(getMinimumValue(subIntervalStart, toReduce, quality));
				reducedInterval.add(getMaximumValue(subIntervalStart, toReduce, quality));
				break;
			default:
				return null;
			}

			subIntervalStart += interval;
			subIntervalEnd += interval;
		}

		return reducedInterval;
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

	private SampledValue getMinimumValue(long subIntervallStart, List<SampledValue> toReduce, Quality quality) {
		if (toReduce.isEmpty()) {
			// always quality bad if list is empty
			return new SampledValue(new DoubleValue(0.f), subIntervallStart, Quality.BAD);
		}
		else {
			double minValue = Double.MAX_VALUE;
			for (SampledValue value : toReduce) {
				if (value.getValue().getDoubleValue() < minValue) {
					minValue = value.getValue().getDoubleValue();
				}
			}
			return new SampledValue(new DoubleValue(minValue), subIntervallStart, quality);
		}
	}

	private SampledValue getMaximumValue(long subIntervallStart, List<SampledValue> toReduce, Quality quality) {
		if (toReduce.isEmpty()) {
			// always quality bad if list is empty
			return new SampledValue(new DoubleValue(0.f), subIntervallStart, Quality.BAD);
		}
		else {
			double maxValue = Double.NEGATIVE_INFINITY;
			for (SampledValue value : toReduce) {
				if (value.getValue().getDoubleValue() > maxValue) {
					maxValue = value.getValue().getDoubleValue();
				}
			}
			return new SampledValue(new DoubleValue(maxValue), subIntervallStart, quality);
		}
	}

	private SampledValue getMeanValue(long timestamp, List<SampledValue> toReduce, Quality quality) {
		if (toReduce.isEmpty()) {
			// always quality bad if list is empty
			return new SampledValue(new DoubleValue(0.f), timestamp, Quality.BAD);
		}
		else {
			double sum = 0L;
			for (SampledValue value : toReduce) {
				try {
					sum += value.getValue().getDoubleValue();
				} catch (IllegalConversionException e) {
					logger.error("", e);
				}
			}
			return new SampledValue(new DoubleValue(sum / toReduce.size()), timestamp, quality);
		}
	}

	@Override
	public void update(RecordedDataConfiguration configuration) throws DataRecorderException {
		this.configuration = configuration;
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
}
