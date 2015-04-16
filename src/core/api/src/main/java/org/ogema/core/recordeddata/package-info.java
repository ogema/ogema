/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3
 * as published by the Free Software Foundation.
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

