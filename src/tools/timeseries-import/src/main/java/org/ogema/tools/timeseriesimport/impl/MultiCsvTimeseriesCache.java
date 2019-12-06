package org.ogema.tools.timeseriesimport.impl;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.tools.timeseriesimport.api.ImportConfiguration;
import org.slf4j.LoggerFactory;

class MultiCsvTimeseriesCache {

	private final Path path;
	private final URL url;
	private final ImportConfiguration config;
	private SoftReference<List<List<SampledValue>>> cached = new SoftReference<List<List<SampledValue>>>(null);
	
	MultiCsvTimeseriesCache(URL url, ImportConfiguration config) {
		this.url = Objects.requireNonNull(url);
		this.config = config;
		this.path = null;
	}
	
	MultiCsvTimeseriesCache(Path path, ImportConfiguration config) {
		this.path = Objects.requireNonNull(path).normalize();
		this.config = config;
		this.url = null;
	}
	
	public CacheAccess getCacheAccess(int idx) {
		return new MultiCacheAccess(idx);
	}
	
	private class MultiCacheAccess implements CacheAccess {
		
		private final int idx;

		public MultiCacheAccess(int idx) {
			this.idx = idx;
		}

		@Override
		public List<SampledValue> getValues(long startTime, long endTime) {
			return MultiCsvTimeseriesCache.this.getValues(startTime, endTime, idx);
		}
		
		@Override
		public SampledValue getFirstValue() {
			try {
				return Utils.getFirstValue(url != null ? url.openStream() : null, path, config, idx);
			} catch (IOException e) {
				LoggerFactory.getLogger(CsvTimeseries.class).error("Failed to parse CSV data from path/url {}", (path != null ? path : url),e);
				return null;
			}
		}
		
		@Override
		public boolean isLoaded() {
			return cached.get() != null;
		}
		
	}
	
	List<SampledValue> getValues(long startTime, long endTime, int idx) {
		if (idx < 0)
			throw new IllegalArgumentException("Index is negative " + idx);
		List<List<SampledValue>> cached=  this.cached.get();
		if (cached == null) {
			synchronized (this) {
				cached=  this.cached.get();
				if (cached == null) {
					try {
						cached = Utils.readMultipleValues(url == null ? null : url.openStream(), path, config, true);
						this.cached = new SoftReference<List<List<SampledValue>>>(cached);
					} catch (IOException e) {
						LoggerFactory.getLogger(CsvTimeseries.class).error("Failed to parse CSV data from path/url {}", (path != null ? path : url),e);
						return Collections.emptyList();
					}
				}
			}
		}
		return getValuesInternal(startTime, endTime, cached, idx);
	}
	
	private List<SampledValue> getValuesInternal(long startTime, long endTime, List<List<SampledValue>> cachedValues, int idx) {
		if (idx >= cachedValues.size())
			return Collections.emptyList();
		final List<SampledValue> cached = cachedValues.get(idx);
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
	
}
