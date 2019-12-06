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
/**
 * Contains non-persistent timeseries implementations:
 * <ul>
 *   <li>ReadOnlyTreeTimeSeries: a read-only timeseries based on a set of data points
 *   	stored internally in a NavigableSet
 *   <li>TreeTimeSeries: like ReadOnlyTreeTimeSeries, but writable
 *   <li>ConcurrentTreeTimeSeries: like TreeTimeSeries, but thread-safe
 *   <li>FunctionTimeSeries: a time series modeled on a java.util.Function
 *   <li>PeriodicTimeSeries: a time series modeled on a finite set of equidistant  
 *   	data points that is repeated over and over again.
 * </ul>
 */
package org.ogema.tools.timeseries.v2.memory;

