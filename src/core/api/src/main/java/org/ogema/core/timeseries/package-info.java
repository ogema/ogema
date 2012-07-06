/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
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

