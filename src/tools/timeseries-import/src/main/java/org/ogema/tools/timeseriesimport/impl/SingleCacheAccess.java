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

class SingleCacheAccess implements CacheAccess {

	private final Path path;
	private final URL url;
	private final ImportConfiguration config;
	private SoftReference<List<SampledValue>> cached = new SoftReference<List<SampledValue>>(null);
	
	SingleCacheAccess(URL url, ImportConfiguration config) {
		this.url = Objects.requireNonNull(url);
		this.config = config;
		this.path = null;
	}
	
	SingleCacheAccess(Path path, ImportConfiguration config) {
		this.path = Objects.requireNonNull(path).normalize();
		this.config = config;
		this.url = null;
	}

	@Override
	public List<SampledValue> getValues(long startTime, long endTime) {
		List<SampledValue> cached=  this.cached.get();
		if (cached == null) {
			synchronized (this) {
				cached=  this.cached.get();
				if (cached == null) {
					try {
						cached = Utils.readValues(url == null ? null : url.openStream(), path, config, true);
						this.cached = new SoftReference<List<SampledValue>>(cached);
					} catch (IOException e) {
						LoggerFactory.getLogger(CsvTimeseries.class).error("Failed to parse CSV data from path/url {}", (path != null ? path : url),e);
						return Collections.emptyList();
					}
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
	public SampledValue getFirstValue() {
		synchronized (this) {
			try {
				return Utils.getFirstValue(url == null ? null : url.openStream(), path, config, 0);
			} catch (IOException e) {
				LoggerFactory.getLogger(CsvTimeseries.class).error("Failed to parse CSV data from path/url {}", (path != null ? path : url),e);
				return null;
			}
		}
	}
	
	@Override
	public boolean isLoaded() {
		return cached.get() != null;
	}
	
}
