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
package org.ogema.tools.timeseriesimport.api;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

import org.ogema.core.timeseries.ReadOnlyTimeSeries;

/**
 * Retrieve an instance as an OSGi service
 */
public interface TimeseriesImport {
	
	List<ReadOnlyTimeSeries> parseMultiple(URL url, ImportConfiguration config, int nrTimeseries) throws IOException;
	
	List<ReadOnlyTimeSeries> parseMultiple(InputStream stream, ImportConfiguration config, int nrTimeseries) throws IOException;
	
	List<ReadOnlyTimeSeries> parseMultiple(Path path, ImportConfiguration config, int nrTimeseries) throws IOException;
	
	/**
	 * 
	 * @param path
	 * @param config
	 * @return
	 * @throws IOException
	 * @throws SecurityException if the caller does not have the read permission for the specified path
	 */
	ReadOnlyTimeSeries parseCsv(Path path, ImportConfiguration config) throws IOException;
	
	/**
	 * @param stream
	 * @param config
	 * @return
	 * @throws IOException
	 * @throws SecurityException if the caller does not have the read permission for the specified path
	 */
	ReadOnlyTimeSeries parseCsv(InputStream stream, ImportConfiguration config) throws IOException;
	
	/**
	 * @param url
	 * @param config
	 * @return
	 * @throws IOException
	 * @throws SecurityException if the caller does not have the read permission for the specified path
	 */
	ReadOnlyTimeSeries parseCsv(URL url, ImportConfiguration config) throws IOException;

}
