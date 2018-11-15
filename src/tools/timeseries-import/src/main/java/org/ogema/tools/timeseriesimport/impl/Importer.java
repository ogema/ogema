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
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.FileUtils;
import org.ogema.core.timeseries.ReadOnlyTimeSeries;
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

	private final Cache<Path, CsvTimeseries> cached = CacheBuilder.newBuilder()
			.softValues()
			.concurrencyLevel(2)
			.removalListener(new RemovalListener<Path, CsvTimeseries>() {

				@Override
				public void onRemoval(RemovalNotification<Path, CsvTimeseries> notification) {
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
	private final Cache<URL, CsvTimeseries> cachedUrls = CacheBuilder.newBuilder()
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
	public ReadOnlyTimeSeries parseCsv(final URL url, final ImportConfiguration config) throws IOException {
		Objects.requireNonNull(url);
		Objects.requireNonNull(config);
		try {
			return cachedUrls.get(url, new Callable<CsvTimeseries>() {

				@Override
				public CsvTimeseries call() throws Exception {
					return new CsvTimeseries(url, config);
				}
			});
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
		try {
			return cached.get(path, new Callable<CsvTimeseries>() {

				@Override
				public CsvTimeseries call() throws Exception {
					return new CsvTimeseries(path, config);
				}
			});
		} catch (ExecutionException e) {
			throw new RuntimeException(e.getCause());
		}
	}
	
	@Override
	public ReadOnlyTimeSeries parseCsv(InputStream stream, ImportConfiguration config) throws IOException {
		Objects.requireNonNull(stream);
		Objects.requireNonNull(config);
		final Path target = Files.createTempFile(tempDir, "timeSeries", ".csv");
		Files.copy(stream, target, StandardCopyOption.REPLACE_EXISTING);
		return parseCsv(target, config);
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
