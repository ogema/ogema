/**
 * Copyright 2011-2019 Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
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
package org.ogema.core.timeseries;

/**
 * Implemented by {@link ReadOnlyTimeSeries} that want to indicate a
 * time interval of interest for a given timestamp.
 * This is particularly relevant for time series consisting of an essentially 
 * inifinite (i.e. very large) number of points, such as a periodic time series
 * which repeats the same set of points over and over again.
 */
public interface TimeSeriesHorizon {

	/**
	 * @param t
	 * @return a timestamp which may be either smaller or larger than the passed timestamp
	 */
	long getProposedHorizon(long t);
	
}
