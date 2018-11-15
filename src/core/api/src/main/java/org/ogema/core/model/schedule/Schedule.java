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
