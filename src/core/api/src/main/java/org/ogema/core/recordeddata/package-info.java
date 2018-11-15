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
/**
 * Definition of the framework data-logging capabilities. Most of the simple
 * single-values resources can be set up for automatic logging by the framework
 * by setting a suitable
 * {@link org.ogema.core.recordeddata.RecordedDataConfiguration} to them. If
 * read out, the logdata are returned in the form of a special
 * {@link org.ogema.core.timeseries.ReadOnlyTimeSeries}, an instance of
 * {@link org.ogema.core.recordeddata.RecordedData}. This object allows direct
 * access to the individual log entries as well as access through a
 * {@link org.ogema.core.recordeddata.ReductionMode} filter.
 */
package org.ogema.core.recordeddata;

