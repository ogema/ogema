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

import java.net.URL;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.core.timeseries.ReadOnlyTimeSeries;
import org.ogema.tools.resource.util.ValueResourceUtils;
import org.ogema.tools.timeseriesimport.api.ImportConfiguration;

/**
 * Parses a CSV file for values and caches the result
 */
// TODO support large files, where caching all values is not an option
class CsvTimeseries implements ReadOnlyTimeSeries {

	private final Path path;
	private final URL url;
	private final ImportConfiguration config;
	private final CacheAccess cacheAccess;
	
	CsvTimeseries(URL url, ImportConfiguration config) {
		this(url, config, new SingleCacheAccess(url, config));
	}
	
	CsvTimeseries(Path path, ImportConfiguration config) {
		this(path, config, new SingleCacheAccess(path, config));
	}
	
	CsvTimeseries(URL url, ImportConfiguration config, CacheAccess cacheAccess) {
		this.url = Objects.requireNonNull(url);
		this.config = config;
		this.path = null;
		this.cacheAccess = cacheAccess;
	}
	
	CsvTimeseries(Path path, ImportConfiguration config, CacheAccess cacheAccess) {
		this.path = Objects.requireNonNull(path).normalize();
		this.config = config;
		this.url = null;
		this.cacheAccess = cacheAccess;
	}
	
	@Override
	public List<SampledValue> getValues(long startTime, long endTime) {
		return cacheAccess.getValues(startTime, endTime);
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
		if (time == Long.MIN_VALUE && !cacheAccess.isLoaded()) {
			return cacheAccess.getFirstValue();
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
		return "CsvTimeseries[" + (path != null ? path.toString() : url.toString()) + "]";
	}
	
}