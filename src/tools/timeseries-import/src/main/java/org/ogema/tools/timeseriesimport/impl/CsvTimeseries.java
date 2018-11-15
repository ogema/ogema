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
package org.ogema.tools.timeseriesimport.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.core.timeseries.ReadOnlyTimeSeries;
import org.ogema.tools.resource.util.ValueResourceUtils;
import org.ogema.tools.timeseriesimport.api.ImportConfiguration;
import org.slf4j.LoggerFactory;

/**
 * Parses a CSV file for values and caches the result
 */
// TODO support large files, where caching all values is not an option
class CsvTimeseries implements ReadOnlyTimeSeries {

	private final Path path;
	private final URL url;
	private final ImportConfiguration config;
	private SoftReference<List<SampledValue>> cached = new SoftReference<List<SampledValue>>(null);
	private final boolean unusualDecimalSeparator;
	
	CsvTimeseries(URL url, ImportConfiguration config) {
		this.url = Objects.requireNonNull(url);
		this.config = config;
		this.path = null;
		this.unusualDecimalSeparator = config.getDecimalSeparator() != '.';
	}
	
	CsvTimeseries(Path path, ImportConfiguration config) {
		this.path = Objects.requireNonNull(path).normalize();
		this.config = config;
		this.url = null;
		this.unusualDecimalSeparator = config.getDecimalSeparator() != '.';
	}
	
	@Override
	public List<SampledValue> getValues(long startTime, long endTime) {
		List<SampledValue> cached=  this.cached.get();
		if (cached == null) {
			synchronized (this) {
				cached=  this.cached.get();
				if (cached == null) {
					// parse CSV file
					cached = AccessController.doPrivileged(new PrivilegedAction<List<SampledValue>>() {

						@Override
						public List<SampledValue> run() {
							final List<SampledValue> cached = new ArrayList<>(100);
							try (final BufferedReader reader = path != null ? Files.newBufferedReader(path, StandardCharsets.UTF_8) : 
									new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
								try (CSVParser parser = new CSVParser(reader, config.getCsvFormat())) {
									long timestamp;
									float value;
									final SimpleDateFormat format = config.getDateTimeFormat();
									final TimeUnit unit = config.getTimeUnit();
									for (CSVRecord record : parser) {
										try {
											if (format != null) {
												timestamp = format.parse(record.get(0)).getTime();
											} else {
												timestamp = Long.parseLong(record.get(0));
												if (unit != TimeUnit.MILLISECONDS) {
													timestamp = TimeUnit.MILLISECONDS.convert(timestamp, unit);
												}
											}
											value = Float.parseFloat(unusualDecimalSeparator ? record.get(1).replace(config.getDecimalSeparator(), '.') : record.get(1));
										} catch (NumberFormatException | ArrayIndexOutOfBoundsException | 
												NullPointerException | ParseException e) {
											continue;
										}
										cached.add(new SampledValue(new FloatValue(value), timestamp, Quality.GOOD));	
									}
								}
							} catch (IOException e) {
								LoggerFactory.getLogger(CsvTimeseries.class).error("Failed to parse CSV data from path {}" ,path,e);
								return Collections.emptyList();
							}
							return cached;
						}
					});
					
					this.cached = new SoftReference<List<SampledValue>>(cached);
				}
			}
		}
		return getValuesInternal(startTime, endTime, cached);
	}
	
	private List<SampledValue> getValuesInternal(long startTime, long endTime, List<SampledValue> cached) {
		if (cached.isEmpty() || startTime <= cached.get(0).getTimestamp() && endTime >= cached.get(cached.size()-1).getTimestamp())
			return Collections.unmodifiableList(cached);
		final List<SampledValue> copy = new ArrayList<>(cached.size());
		for (SampledValue sv : cached) {
			final long t = sv.getTimestamp();
			if (t < startTime)
				continue;
			if (t >= endTime)
				break;
			copy.add(sv);
		}
		return copy;
	}
	
	@Override
	public List<SampledValue> getValues(long startTime) {
		return getValues(startTime, Long.MAX_VALUE);
	}
	
	@Override
	public int size() {
		return size(Long.MIN_VALUE, Long.MAX_VALUE);
	}
	
	@Override
	public int size(long startTime, long endTime) {
		return getValues(startTime, endTime).size();
	}
	
	@Override
	public SampledValue getValue(long time) {
		final InterpolationMode mode = config.getInterpolationMode();
		if (mode == null || mode == InterpolationMode.NONE) {
			final Iterator<SampledValue> it = iterator(time, time);
			return it.hasNext() ? it.next() : null;
		}
		final Iterator<SampledValue> it = iterator(Long.MIN_VALUE, time);
		if (!it.hasNext())
			return null;
		SampledValue last = it.next();
		if (last.getTimestamp() == time)
			return last;
		if (last.getTimestamp() > time)
			return null;
		while (it.hasNext()) {
			SampledValue sv = it.next();
			if (sv.getTimestamp() == time)
				return sv;
			if (sv.getTimestamp() > time)
				return ValueResourceUtils.interpolate(last, sv, time, mode);
			last = sv;
		}
		if (mode == InterpolationMode.STEPS || mode == InterpolationMode.NEAREST)
			return new SampledValue(last.getValue(), time, last.getQuality());
		return null;
	}
	
	@Override
	public SampledValue getNextValue(long time) {
		// special case: determine first value only should not require reading the whole file
		if (time == Long.MIN_VALUE && this.cached.get() == null) {
			synchronized (this) {
				return AccessController.doPrivileged(new PrivilegedAction<SampledValue>() {

					@Override
					public SampledValue run() {
						try (final BufferedReader reader = path != null ? Files.newBufferedReader(path, StandardCharsets.UTF_8) : 
							new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
							try (CSVParser parser = new CSVParser(reader, config.getCsvFormat())) {
								long timestamp;
								float value;
								final SimpleDateFormat format = config.getDateTimeFormat();
								final TimeUnit unit = config.getTimeUnit();
								for (CSVRecord record : parser) {
									try {
										if (format != null) {
											timestamp = format.parse(record.get(0)).getTime();
										} else {
											timestamp = Long.parseLong(record.get(0));
											if (unit != TimeUnit.MILLISECONDS) {
												timestamp = TimeUnit.MILLISECONDS.convert(timestamp, unit);
											}
										}
										value = Float.parseFloat(unusualDecimalSeparator ? record.get(1).replace(config.getDecimalSeparator(), '.') : record.get(1));
										return new SampledValue(new FloatValue(value), timestamp, Quality.GOOD);
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
		final Iterator<SampledValue> it = iterator(time, Long.MAX_VALUE);
		return it.hasNext() ? it.next() : null;
	}
	
	@Override
	public SampledValue getPreviousValue(long time) {
		final Iterator<SampledValue> it = iterator(Long.MIN_VALUE, time);
		if (!it.hasNext())
			return null;
		SampledValue last = it.next();
		if (last.getTimestamp() == time)
			return last;
		if (last.getTimestamp() > time)
			return null;
		while (it.hasNext()) {
			final SampledValue next = it.next();
			if (next.getTimestamp() == time)
				return next;
			if (next.getTimestamp() > time)
				return last;
			last = next;
		}
		return last;
	}
	
	@Override
	public Iterator<SampledValue> iterator() {
		return iterator(Long.MIN_VALUE, Long.MAX_VALUE);
	}
	
	@Override
	public Iterator<SampledValue> iterator(long startTime, long endTime) {
		return getValues(startTime, endTime).iterator(); 
	}
	
	@Override
	public InterpolationMode getInterpolationMode() {
		return config.getInterpolationMode();
	}
	
	@Override
	public Long getTimeOfLatestEntry() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean isEmpty() {
		return !iterator().hasNext();
	}
	
	@Override
	public boolean isEmpty(long startTime, long endTime) {
		return !iterator(startTime, endTime).hasNext();
	}
	
	public String getPath() {
		return path != null ? path.toString() : url.getPath();
	}
	
	@Override
	public String toString() {
		return "CsvTimeseries[" + path.toString() + "]";
	}
	
}