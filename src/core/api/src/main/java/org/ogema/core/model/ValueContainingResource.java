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
package org.ogema.core.model;

import org.ogema.core.model.array.ArrayResource;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.core.timeseries.TimeSeries;

/**
 * This is a marker interface for resource types whose nodes not only define structure
 * but also contain actual values. Three different specializations of this exist:
 * {@link SingleValueResource} for nodes containing a single value, {@link ArrayResource}
 * for nodes containing an array of nodes, and {@link Schedule} for nodes containing
 * a full {@link TimeSeries}.
 */
public interface ValueContainingResource extends Resource {

	/**
	 * Gets the time of the most recent write access to the resource. Write
	 * accesses are counted as an update even when the new value equals the
	 * old one.
	 * @return timestamp of the last write access to the resource in ms since 1970.  If the resource has never been written to so far, this returns -1.
	 */
	long getLastUpdateTime();
}
