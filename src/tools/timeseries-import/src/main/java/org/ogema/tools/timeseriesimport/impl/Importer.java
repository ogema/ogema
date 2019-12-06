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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import org.apache.commons.io.FileUtils;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.timeseries.ReadOnlyTimeSeries;
import org.ogema.tools.timeseries.api.FloatTimeSeries;
import org.ogema.tools.timeseries.implementations.FloatTreeTimeSeries;
import org.ogema.tools.timeseriesimport.api.ImportConfiguration;
import org.ogema.tools.timeseriesimport.api.TimeseriesImport;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

@Component(service=TimeseriesImport.class)
public class Importer implements TimeseriesImport {

	private final Cache<Path, List<ReadOnlyTimeSeries>> cached = CacheBuilder.newBuilder()
			.softValues()
			.concurrencyLevel(2)
			.removalListener(new RemovalListener<Path, List<ReadOnlyTimeSeries>>() {

				@Override
				public void onRemoval(RemovalNotification<Path, List<ReadOnlyTimeSeries>> notification) {
					try {
						final Path path = notification.getKey();
						if (path.startsWith(tempDir))
							Files.delete(path);
					} catch (Exception e) {
						LoggerFactory.getLogger(TimeseriesImport.class).warn("Failed to delete temp file " + notification.getKey(),e);
					}
				}
			})
			.build();
	private final Cache<URL, List<ReadOnlyTimeSeries>> cachedUrls = CacheBuilder.newBuilder()
			.softValues()
			.concurrencyLevel(2)
			.build();
	private volatile Path tempDir;
	
	@Activate
	protected void activate(BundleContext ctx) {
		this.tempDir = ctx.getDataFile("temp").toPath();
		cleanUpTempFolder(tempDir);
	}

	@Deactivate
	protected void deactivate() {
		cleanUpTempFolder(tempDir);
	}
	
	@Override
	public List<ReadOnlyTimeSeries> parseMultiple(URL url, ImportConfiguration config, int nrTimeseries) throws IOException {
		Objects.requireNonNull(url);
		Objects.requireNonNull(config);
		if (nrTimeseries < 0)
			throw new IllegalArgumentException("nr timeseries < 0: " + nrTimeseries);
		if (nrTimeseries == 0)
			return Collections.emptyList();
		final List<Integer> valueIndices = config.getValueIndices();
		if (config.isParseEagerly() || valueIndices == null || valueIndices.isEmpty())
			return eagerMultiImport(url.openStream(), null, config);
		final MultiCsvTimeseriesCache cache = new MultiCsvTimeseriesCache(url, config);
		final List<ReadOnlyTimeSeries> list = new ArrayList<>(nrTimeseries);
		for (int i=0; i<nrTimeseries; i++) {
			list.add(new CsvTimeseries(url, config, cache.getCacheAccess(i)));
		}
		return list;
	}
	
	@Override
	public List<ReadOnlyTimeSeries> parseMultiple(Path path, ImportConfiguration config, int nrTimeseries) throws IOException {
		Objects.requireNonNull(path);
		Objects.requireNonNull(config);
		if (nrTimeseries < 0)
			throw new IllegalArgumentException("nr timeseries < 0: " + nrTimeseries);
		if (nrTimeseries == 0)
			return Collections.emptyList();
		final List<Integer> valueIndices = config.getValueIndices();
		if (config.isParseEagerly() || valueIndices == null || valueIndices.isEmpty())
			return eagerMultiImport(null, path, config);
		final MultiCsvTimeseriesCache cache = new MultiCsvTimeseriesCache(path, config);
		final List<ReadOnlyTimeSeries> list = new ArrayList<>(nrTimeseries);
		for (int i=0; i<nrTimeseries; i++) {
			list.add(new CsvTimeseries(path, config, cache.getCacheAccess(i)));
		}
		return list;
	}
	
	@Override
	public List<ReadOnlyTimeSeries> parseMultiple(InputStream stream, ImportConfiguration config, int nrTimeserie) throws IOException {
		Objects.requireNonNull(stream);
		Objects.requireNonNull(config);
		if (config.isParseEagerly())
			return eagerMultiImport(stream, null, config);
		final Path target = Files.createTempFile(tempDir, "timeSeries", ".csv");
		Files.copy(stream, target, StandardCopyOption.REPLACE_EXISTING);
		return parseMultiple(target, config, nrTimeserie);
	}
	
	@Override
	public ReadOnlyTimeSeries parseCsv(final URL url, final ImportConfiguration config) throws IOException {
		Objects.requireNonNull(url);
		Objects.requireNonNull(config);
		if (config.isParseEagerly())
			return eagerImport(url.openStream(), null, config);
		try {
			return cachedUrls.get(url, new Callable<List<ReadOnlyTimeSeries>>() {

				@Override
				public List<ReadOnlyTimeSeries> call() throws Exception {
					return Collections.<ReadOnlyTimeSeries> singletonList(new CsvTimeseries(url, config));
				}
			}).get(0);
		} catch (ExecutionException e) {
			throw new RuntimeException(e.getCause());
		}
	}
	
	@Override
	public ReadOnlyTimeSeries parseCsv(final Path path, final ImportConfiguration config) throws IOException {
		Objects.requireNonNull(path);
		Objects.requireNonNull(config);
		if (!Files.exists(path))
			throw new FileNotFoundException("File " + path + " does not exist");
		if (config.isParseEagerly())
			return eagerImport(null, path, config);
		try {
			return cached.get(path, new Callable<List<ReadOnlyTimeSeries>>() {

				@Override
				public List<ReadOnlyTimeSeries> call() throws Exception {
					return Collections.<ReadOnlyTimeSeries> singletonList(new CsvTimeseries(path, config));
				}
			}).get(0);
		} catch (ExecutionException e) {
			throw new RuntimeException(e.getCause());
		}
	}
	
	@Override
	public ReadOnlyTimeSeries parseCsv(InputStream stream, ImportConfiguration config) throws IOException {
		Objects.requireNonNull(stream);
		Objects.requireNonNull(config);
		if (config.isParseEagerly())
			return eagerImport(stream, null, config);
		final Path target = Files.createTempFile(tempDir, "timeSeries", ".csv");
		Files.copy(stream, target, StandardCopyOption.REPLACE_EXISTING);
		return parseCsv(target, config);
	}
	
	private static ReadOnlyTimeSeries eagerImport(final InputStream stream, final Path path, final ImportConfiguration config) {
		final FloatTimeSeries result = new FloatTreeTimeSeries();
		result.addValues(Utils.readValues(stream, path, config, false));
		result.setInterpolationMode(config.getInterpolationMode());
		return result;
	}
	
	private static List<ReadOnlyTimeSeries> eagerMultiImport(final InputStream stream, final Path path, final ImportConfiguration config) {
		final List<List<SampledValue>> timeseries = Utils.readMultipleValues(stream, path, config, false);
		if (timeseries.isEmpty())
			return Collections.emptyList();
		final List<ReadOnlyTimeSeries> list = new ArrayList<>(timeseries.size());
		for (final List<SampledValue> values : timeseries) {
			final FloatTimeSeries result = new FloatTreeTimeSeries();
			result.addValues(values);
			result.setInterpolationMode(config.getInterpolationMode());
			list.add(result);
		}
		return list;
	}
	
	private static void cleanUpTempFolder(final Path path) {
		if (path == null || !Files.isDirectory(path))
			return;
		try {
			FileUtils.deleteDirectory(path.toFile());
		} catch (Exception e) {
			LoggerFactory.getLogger(TimeseriesImport.class).warn("Failed to clean up temp directory",e);
		}
	}
	
}
