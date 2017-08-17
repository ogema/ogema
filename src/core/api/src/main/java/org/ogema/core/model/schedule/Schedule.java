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
package org.ogema.core.model.schedule;

import java.util.Iterator;

import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.model.ValueResource;
import org.ogema.core.timeseries.TimeSeries;

/**
 * A schedule contains data that is relevant for the future behavior of the resource to which it is attached. Depending
 * on the the actual type of the schedule, this may be a forecast of the value or a definition (e.g. a price profile, which is defined
 * by a source outside of the OGEMA gateway). <br>
 * 
 * Schedules can hold values of exactly one type, only (i.e. FloatValue or IntegerValue but not both). The type
 * of values a schedule accepts is defined by the type of the simple resource the schedule is attached to. <br>
 */
public interface Schedule extends ValueResource, TimeSeries {
	
	/**
	 * Get an iterator over all points in the schedule.<br>
	 * This iterator does not throw ConcurrentModificationException. If the schedule is changed during iteration, 
	 * changes may or may not be reflected by the iterator. The returned values are always ordered chronologically,
	 * though. <br>
	 * The iterator does not support the <tt>remove</tt>-method.
	 * @return
	 */
	@Override
	Iterator<SampledValue> iterator();
	
	/**
	 * Get an iterator over all points in the requested interval
	 * This iterator does not throw ConcurrentModificationException. If the schedule is changed during iteration, 
	 * changes may or may not be reflected by the iterator. The returned values are always ordered chronologically,
	 * though. <br>
	 * The iterator does not support the <tt>remove</tt>-method.
	 * @param startTime 
	 * 			Start time of the interval. Inclusive. 
	 * @param endTime 
	 * 			End time of the interval. Inclusive.
	 * @return
	 */
	@Override
	Iterator<SampledValue> iterator(long startTime, long endTime);
	
}
