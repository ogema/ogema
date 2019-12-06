package org.ogema.tools.timeseriesimport.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.tools.timeseriesimport.api.ImportConfiguration;
import org.slf4j.LoggerFactory;

class Utils {
	
	// returns list of timeseries
	public static List<List<SampledValue>> readMultipleValues(final InputStream stream, final Path path, final ImportConfiguration config, final boolean privileged) {
		if (stream == null && path == null)
			throw new NullPointerException("Either stream or path must be non-null");
		final PrivilegedAction<List<List<SampledValue>>> action = new PrivilegedAction<List<List<SampledValue>>>() {

			@Override
			public List<List<SampledValue>> run() {
				try (final BufferedReader reader = 
						stream != null ? new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)) :
						Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
					try (CSVParser parser = new CSVParser(reader, config.getCsvFormat())) {
						long timestamp;
						float value;
						final SimpleDateFormat format = config.getDateTimeFormat();
						final TimeUnit unit = config.getTimeUnit();
						final boolean unusualDecimalSeparator = config.getDecimalSeparator() != '.';
						final int timeIndex = config.getTimeIndex();
						final List<Integer> valueIndices = config.getValueIndices();
						final Long startTime = config.getStartTime();
						final Long interval = config.getInterval();
						final float factor = config.getFactor();
						final float addend = config.getAddend();
						Long timeOffset = null;
						int cnt = 0;
						final List<List<SampledValue>> timeseries = new ArrayList<>(valueIndices != null ? valueIndices.size() : 4);
//						final List<SampledValue> values = new ArrayList<SampledValue>(100);
						for (CSVRecord record : parser) {
							try {
								if (timeIndex >= 0) {
									if (format != null) {
										timestamp = format.parse(record.get(timeIndex)).getTime();
									} else {
										timestamp = Long.parseLong(record.get(timeIndex));
										if (unit != TimeUnit.MILLISECONDS) {
											timestamp = TimeUnit.MILLISECONDS.convert(timestamp, unit);
										}
									}
									if (startTime != null) {
										if (timeOffset == null) // first iteration
											timeOffset = startTime - timestamp;
										timestamp += timeOffset;
									}
								} else {
									timestamp = startTime + cnt++ * interval;
								}
								int timeseriesCnt = 0;
								for (int i = 0; i < record.size(); i++) {
									if (i == timeIndex || (valueIndices != null && !valueIndices.contains(i)))
										continue;
									final int idx = timeseriesCnt++;
									if (idx >= timeseries.size())
										timeseries.add(new ArrayList<SampledValue>(100));
									final String entry = record.get(i).trim();
									if (!entry.isEmpty()) {
										value = Float.parseFloat(unusualDecimalSeparator ? entry.replace(config.getDecimalSeparator(), '.') : entry);
										timeseries.get(idx).add(new SampledValue(new FloatValue(value * factor + addend), timestamp, Quality.GOOD));
									}
								}
							} catch (NumberFormatException | ArrayIndexOutOfBoundsException | 
									NullPointerException | ParseException e) {
								continue;
							}
						}
						return timeseries;
					}
				} catch (IOException e) {
					LoggerFactory.getLogger(CsvTimeseries.class).error("Failed to parse CSV data.",e);
					return Collections.emptyList();
				}
			}
		};
		return privileged ? AccessController.doPrivileged(action) : action.run();
	}
	
	public static List<SampledValue> readValues(final InputStream stream, final Path path, final ImportConfiguration config, final boolean privileged) {
		if (stream == null && path == null)
			throw new NullPointerException("Either stream or path must be non-null");
		final PrivilegedAction<List<SampledValue>> action = new PrivilegedAction<List<SampledValue>>() {

			@Override
			public List<SampledValue> run() {
				try (final BufferedReader reader = 
						stream != null ? new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)) :
						Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
					try (CSVParser parser = new CSVParser(reader, config.getCsvFormat())) {
						final List<SampledValue> cached = new ArrayList<>(100);
						long timestamp;
						float value;
						final SimpleDateFormat format = config.getDateTimeFormat();
						final TimeUnit unit = config.getTimeUnit();
						final boolean unusualDecimalSeparator = config.getDecimalSeparator() != '.';
						final int timeIndex = config.getTimeIndex();
						final int valueIndex = config.getValueIndex();
						final Long startTime = config.getStartTime();
						final Long interval = config.getInterval();
						final float factor = config.getFactor();
						final float addend = config.getAddend();
						Long timeOffset = null;
						int cnt = 0;
						for (CSVRecord record : parser) {
							try {
								value = Float.parseFloat(unusualDecimalSeparator ? record.get(valueIndex).replace(config.getDecimalSeparator(), '.') : record.get(valueIndex));
								if (timeIndex >= 0) {
									if (format != null) {
										timestamp = format.parse(record.get(timeIndex)).getTime();
									} else {
										timestamp = Long.parseLong(record.get(timeIndex));
										if (unit != TimeUnit.MILLISECONDS) {
											timestamp = TimeUnit.MILLISECONDS.convert(timestamp, unit);
										}
									}
									if (startTime != null) {
										if (timeOffset == null) // first iteration
											timeOffset = startTime - timestamp;
										timestamp += timeOffset;
									}
								} else {
									timestamp = startTime + cnt++ * interval;
								}
							} catch (NumberFormatException | ArrayIndexOutOfBoundsException | 
									NullPointerException | ParseException e) {
								continue;
							}
							cached.add(new SampledValue(new FloatValue(value * factor + addend), timestamp, Quality.GOOD));	
						}
						return cached;
					}
				} catch (IOException e) {
					LoggerFactory.getLogger(CsvTimeseries.class).error("Failed to parse CSV data.",e);
					return Collections.emptyList();
				}
			}
		};
		return privileged ? AccessController.doPrivileged(action) : action.run();
	}
	
	public static SampledValue getFirstValue(final InputStream stream, final Path path, final ImportConfiguration config, final int timeseriesIdx) {
		return AccessController.doPrivileged(new PrivilegedAction<SampledValue>() {

			@Override
			public SampledValue run() {
				try (final BufferedReader reader = 
						stream != null ? new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)) :
						Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
					try (CSVParser parser = new CSVParser(reader, config.getCsvFormat())) {
						long timestamp;
						float value;
						final SimpleDateFormat format = config.getDateTimeFormat();
						final TimeUnit unit = config.getTimeUnit();
						final boolean unusualDecimalSeparator = config.getDecimalSeparator() != '.';
						final int timeIndex = config.getTimeIndex();
						final int valueIndex = config.getValueIndex();
						final Long startTime = config.getStartTime();
						final Long interval = config.getInterval();
						final float factor = config.getFactor();
						final float addend = config.getAddend();
						final List<Integer> valueIndices = config.getValueIndices() != null ? config.getValueIndices() : 
							valueIndex >= 0 ? Collections.singletonList(valueIndex) : null;
						Long timeOffset = null;
						int cnt = 0;
						for (CSVRecord record : parser) {
							try {
								if (timeIndex >= 0) {
									if (format != null) {
										timestamp = format.parse(record.get(timeIndex)).getTime();
									} else {
										timestamp = Long.parseLong(record.get(timeIndex));
										if (unit != TimeUnit.MILLISECONDS) {
											timestamp = TimeUnit.MILLISECONDS.convert(timestamp, unit);
										}
									}
									if (startTime != null) {
										if (timeOffset == null) // first iteration
											timeOffset = startTime - timestamp;
										timestamp += timeOffset;
									}
								} else {
									timestamp = startTime + cnt++ * interval;
								}
								int timeseriesCnt = 0;
								for (int i = 0; i < record.size(); i++) {
									if (i == timeIndex || (valueIndices != null && !valueIndices.contains(i)))
										continue;
									if (timeseriesCnt++ < timeseriesIdx)
										continue;
									final String entry = record.get(i).trim();
									if (entry.isEmpty())
										break;
									value = Float.parseFloat(unusualDecimalSeparator ? entry.replace(config.getDecimalSeparator(), '.') : entry);
									return new SampledValue(new FloatValue(value * factor + addend), timestamp, Quality.GOOD);
								}
								
							} catch (NumberFormatException | ArrayIndexOutOfBoundsException | 
									NullPointerException | ParseException e) {
								continue;
							}
						}
					}
				} catch (IOException e) {
					LoggerFactory.getLogger(CsvTimeseries.class).error("Failed to parse CSV data from path {}" ,path,e);
				}
				return null; // if we arrive here at all, then there are no values to be read
			}
		});
	}

}
