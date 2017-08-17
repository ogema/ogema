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
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.ogema.core.channelmanager.measurements.IllegalConversionException;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.recordeddata.RecordedDataConfiguration;
import org.ogema.core.recordeddata.ReductionMode;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.recordeddata.DataRecorderException;
import org.ogema.recordeddata.RecordedDataStorage;
import org.ogema.recordeddata.slotsdb.reduction.Reduction;
import org.ogema.recordeddata.slotsdb.reduction.ReductionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SlotsDbStorage implements RecordedDataStorage {

	// guarded by lock
	private RecordedDataConfiguration configuration;
	private final String id;
	private final SlotsDb recorder;
	
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	
	private final static Logger logger = LoggerFactory.getLogger(SlotsDbStorage.class);

	public SlotsDbStorage(String id, RecordedDataConfiguration configuration, SlotsDb recorder) {
		this.configuration = configuration;
		this.id = id;
		this.recorder = recorder;
	}
	
	@Override
	public String getPath() {
		return id;
	}

	@Override
	public void insertValue(final SampledValue value) throws DataRecorderException {

		try {
			AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {

				@Override
				public Void run() throws Exception {
					lock.writeLock().lock();
					try {
						if (configuration != null) {
							recorder.getProxy().appendValue(id, value.getValue().getDoubleValue(), value.getTimestamp(),
									(byte) value.getQuality().getQuality(), configuration);
						}
					} catch (IOException e) {
						logger.error("", e);
					} catch (IllegalConversionException e) {
						logger.error("", e);
					} catch (NullPointerException e) {
						logger.error("NPE in SlotsdbStorage - recorder: {}, proxy: {}, id: {}, config: {}, value: {}", 
								recorder, (recorder!=null? recorder.getProxy(): null), id, configuration, value,e);
					} finally {
						lock.writeLock().unlock();
					}

					return null;
				}

			});
		} catch (PrivilegedActionException e) {
			logger.error("", e);
		}

	}

	@Override
	public void insertValues(final List<SampledValue> values) throws DataRecorderException {

		try {
			AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {

				@Override
				public Void run() throws Exception {
					lock.writeLock().lock();
					try {
						if (configuration != null) {

							for (SampledValue value : values) {
								recorder.getProxy().appendValue(id, value.getValue().getDoubleValue(), value.getTimestamp(),
										(byte) value.getQuality().getQuality(), configuration);
							}
						}
					} catch (IOException e) {
						logger.error("", e);
					} catch (IllegalConversionException e) {
						logger.error("", e);
					} finally {
						lock.writeLock().unlock();
					}
					return null;
				}

			});
		} catch (PrivilegedActionException e) {
			logger.error("", e);
		}
	}

	@Override
	public List<SampledValue> getValues(final long startTime) {

		try {
			return AccessController
					.doPrivileged(new PrivilegedExceptionAction<List<SampledValue>>() {

						@Override
						public List<SampledValue> run() throws Exception {

							List<SampledValue> records = null;
							lock.readLock().lock();
							try {
								// long storageInterval = configuration.getFixedInterval();
//								records = recorder.getProxy().read(id, startTime, System.currentTimeMillis(), configuration);
								if (recorder.clock != null)
									records = recorder.getProxy().read(id, startTime, recorder.clock.getExecutionTime(), configuration);
								else // only relevant for tests
									records = recorder.getProxy().read(id, startTime,  System.currentTimeMillis(), configuration);
							} catch (IOException e) {
								logger.error("", e);
							} finally {
								lock.readLock().unlock();
							}

							return records;
						}

					});

		} catch (PrivilegedActionException e) {
			logger.error("", e);
			return null;
		}

	}

	@Override
	public List<SampledValue> getValues(final long startTime, final long endTime) {

		try {
			return AccessController
					.doPrivileged(new PrivilegedExceptionAction<List<SampledValue>>() {

						@Override
						public List<SampledValue> run() throws Exception {

							// --------------------------

							List<SampledValue> records = null;
							lock.readLock().lock();
							try {
								records = recorder.getProxy().read(id, startTime, endTime - 1, configuration);
							} catch (IOException e) {
								logger.error("", e);
							} finally {
								lock.readLock().unlock();
							}
							return records;

							// --------------------------

						}

					});

		} catch (PrivilegedActionException e) {
			logger.error("", e);
			return null;
		}

	}

	@Override
	public SampledValue getValue(final long timestamp) {

		try {
			return AccessController.doPrivileged(new PrivilegedExceptionAction<SampledValue>() {

				@Override
				public SampledValue run() throws Exception {

					// ------
					lock.readLock().lock();
					try {
						return recorder.getProxy().read(id, timestamp, configuration);
					} catch (IOException e) {
						logger.error("", e);
						return null;
					} finally {
						lock.readLock().unlock();
					}

					// ------
				}

			});

		} catch (PrivilegedActionException e) {
			logger.error("", e);
			return null;
		}

	}

	// TODO more efficient implementation; might as well return an iterator?
	@Override
	public List<SampledValue> getValues(final long startTime, final long endTime, final long intervalSize,
			final ReductionMode mode) {

		// last timestamp is exclusive and therefore not part of the request
		final long endTimeMinusOne = endTime - 1;

		try {
			return AccessController
					.doPrivileged(new PrivilegedExceptionAction<List<SampledValue>>() {

						@Override
						public List<SampledValue> run() throws Exception {

							// ----------------

							// TODO could cause crash? extremely long requested time period of very small sampled values
							List<SampledValue> returnValues = new ArrayList<SampledValue>();

							if (validateArguments(startTime, endTimeMinusOne, intervalSize)) {

								// Issues to consider:
								// a) calling the for each subinterval will slow down the reduction (many file accesses)
								// b) calling the read for the whole requested time period might be problematic -
								// especially when requested
								// time
								// period is large and the log interval was very small (large number of values)
								// Compromise: When the requested time period covers multiple days and therefore
								// multiple log files, then a
								// separate read data processing is performed for each file.
								final List<SampledValue> loggedValues;
								lock.readLock().lock();
								try {
									loggedValues = getLoggedValues(startTime, endTimeMinusOne);
								} finally {
									lock.readLock().unlock();
								}
//								List<SampledValue> loggedValues = removeQualityBad(loggedValuesRaw);

								if (loggedValues.isEmpty()) {
									// return an empty list since there are no logged values, so it doesn't make sense
									// to aggregate anything
									return returnValues;
								}

								if (mode.equals(ReductionMode.NONE)) {
									return removeQualityBad(loggedValues);
								}

								List<Interval> intervals = generateIntervals(startTime, endTimeMinusOne, intervalSize);
								returnValues = generateReducedData(intervals, loggedValues.iterator(), mode);

							}

							return returnValues;

							// ----------------
						}

					});

		} catch (PrivilegedActionException e) {
			logger.error("", e);
			return null;
		}
	}

	/**
	 * Generates equidistant intervals starting from periodStart till periodEnd. The last interval might have a
	 * different length than intervalSize.
	 * 
	 * ASSUMPTION: Arguments are valid (see validateArguments method)
	 * 
	 * @return List of intervals which cover the entire period
	 */
	private static List<Interval> generateIntervals(long periodStart, long periodEnd, long intervalSize) {

		final List<Interval> intervals = new ArrayList<Interval>();

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

	private static List<SampledValue> generateReducedData(List<Interval> intervals, List<SampledValue> loggedValues, ReductionMode mode) {

		final List<SampledValue> returnValues = new ArrayList<SampledValue>();
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

				if (loggedValue.getQuality() == Quality.GOOD)
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

		// debug_printIntervals(intervals);

		return returnValues;
	}
	
	// ignores bad quality values
	private static List<SampledValue> generateReducedData(List<Interval> intervals, Iterator<SampledValue> loggedValues, ReductionMode mode) {

		final List<SampledValue> returnValues = new ArrayList<SampledValue>();
		if (!loggedValues.hasNext())
			return returnValues;
		List<SampledValue> reducedValues;

		final ReductionFactory reductionFactory = new ReductionFactory();
		final Reduction reduction = reductionFactory.getReduction(mode);

		
		SampledValue loggedValue = loggedValues.next();
		long timestamp = loggedValue.getTimestamp();

		Iterator<Interval> it = intervals.iterator();
		Interval interval;
		boolean done = false;
		while (it.hasNext()) {

			interval = it.next();
			
			while (   timestamp >= interval.getStart() && timestamp <= interval.getEnd()) {

				if (loggedValue.getQuality() == Quality.GOOD)
					interval.add(loggedValue);
				if (!loggedValues.hasNext()) {
					done = true;
					break;
				}
				loggedValue = loggedValues.next();
				timestamp = loggedValue.getTimestamp();
				
			}

			reducedValues = reduction.performReduction(interval.getValues(), interval.getStart());
			returnValues.addAll(reducedValues);
			if (done) 
				break;
		}
		while (it.hasNext()) {
			interval = it.next();
			reducedValues = reduction.performReduction(interval.getValues(), interval.getStart());
			returnValues.addAll(reducedValues);
		}

		// debug_printIntervals(intervals);

		return returnValues;
	}

	
	@SuppressWarnings("unused")
	private static void debug_printIntervals(List<Interval> intervals) {

		Iterator<Interval> it2 = intervals.iterator();
		Interval interval2;

		int i = 1;
		while (it2.hasNext()) {
			interval2 = it2.next();
			System.out.println(i + ": " + interval2.getValues().toString());
			i++;
		}
	}

	private static boolean validateArguments(long startTime, long endTime, long interval) {
		boolean result = false;

		if (startTime > endTime) {
			logger.warn("Invalid parameters: Start timestamp musst be smaller than end timestamp");
		}
		else if (interval < 0) {
			logger.warn("Invalid arguments: interval must be > 0");
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
			List<SampledValue> loggedValues = recorder.getProxy().read(id, startTime, endTime, configuration);
			return loggedValues;
		} catch (IOException e) {
			e.printStackTrace();
			return new ArrayList<SampledValue>();
		}
	}

	private static List<SampledValue> removeQualityBad(List<SampledValue> toReduce) {
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
		setConfiguration(configuration);
	}

	@Override
	public void setConfiguration(RecordedDataConfiguration configuration) {
		lock.writeLock().lock();

		try {
			this.configuration = configuration;
			AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {

				@Override
				public Void run() throws Exception {

					// -----------

					recorder.persistSlotsDbStorages();

					// -----------

					return null;
				}

			});
		} catch (PrivilegedActionException e) {
			logger.error("", e);
		} finally {
			lock.writeLock().unlock();
		}

	}

	@Override
	public RecordedDataConfiguration getConfiguration() {
		return configuration;
	}

	@Override
	public SampledValue getNextValue(final long time) {

		try {
			return AccessController.doPrivileged(new PrivilegedExceptionAction<SampledValue>() {

				@Override
				public SampledValue run() throws Exception {

					// ------
					lock.readLock().lock();
					try {
						return recorder.getProxy().readNextValue(id, time, configuration);
					} catch (IOException e) {
						logger.error("", e);
						return null;
					} finally {
						lock.readLock().unlock();
					}

					// ------
				}

			});

		} catch (PrivilegedActionException e) {
			logger.error("", e);
			return null;
		}

	}
	
	@Override
	public SampledValue getPreviousValue(final long time) {
		try {
			return AccessController.doPrivileged(new PrivilegedExceptionAction<SampledValue>() {

				@Override
				public SampledValue run() throws Exception {

					// ------
					lock.readLock().lock();
					try {
						return recorder.getProxy().readPreviousValue(id, time, configuration);
					} catch (IOException e) {
						logger.error("", e);
						return null;
					} finally {
						lock.readLock().unlock();
					}

					// ------
				}

			});

		} catch (PrivilegedActionException e) {
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

	@Override
	public boolean isEmpty() {
		return getNextValue(Long.MIN_VALUE) == null;
	}

	@Override
	public boolean isEmpty(long startTime, long endTime) {
		SampledValue sv = getNextValue(startTime);
		return (sv == null || sv.getTimestamp() > endTime);
	}

	@Override
	public int size() {
		return size(Long.MIN_VALUE, Long.MAX_VALUE);
	}

	@Override
	public int size(final long startTime, final long endTime) {
		try {
			return AccessController.doPrivileged(new PrivilegedExceptionAction<Integer>() {

				@Override
				public Integer run() throws Exception {
					lock.readLock().lock();
					try {
						return recorder.getProxy().size(id, startTime, endTime);
					} finally {
						lock.readLock().unlock();
					}
				}

			});

		} catch (PrivilegedActionException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Iterator<SampledValue> iterator() {
		return new SlotsDbIterator(id, recorder, lock);
	}

	@Override
	public Iterator<SampledValue> iterator(long startTime, long endTime) {
		return new SlotsDbIterator(id, recorder, lock, startTime, endTime);
	}

}

class Interval {

	final List<SampledValue> values = new ArrayList<SampledValue>();

	private final long start;
	private final long end;

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
