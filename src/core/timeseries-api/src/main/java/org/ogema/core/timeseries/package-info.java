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
 * Definition of {@link org.ogema.core.timeseries.TimeSeries}, which are
 * functions over time. Time series are modeled as a set of support points of
 * type {@link org.ogema.core.channelmanager.measurements.SampledValue} (the
 * entries) together with an {@link org.ogema.core.timeseries.InterpolationMode}
 * that defines the values of the function between the support points. Limited
 * definition ranges of the time series, including gaps in the definition range,
 * can be modeled using a bad
 * {@link org.ogema.core.channelmanager.measurements.Quality}. If the quality of
 * a time series is bad, the time series should be considered as not being
 * (sensibly) defined for this time.
 */
package org.ogema.core.timeseries;

