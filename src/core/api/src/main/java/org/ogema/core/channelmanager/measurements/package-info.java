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
//TODO: links
/**
 * Definitions for the different value types that can be passed 
 * via the OGEMA ChannelManager and used in {@link org.ogema.core.timeseries.TimeSeries}.
 * The value types implement the interface {@link Value}
 * for different types of values. Also defines the {@link SampledValue} which is a Value
 * combined with a timestamp and a {@link Quality}. The SampledValue is intended to be used
 * for measured values and for use in {@link org.ogema.core.timeseries.TimeSeries} (defined in another package).
 */
package org.ogema.core.channelmanager.measurements;

