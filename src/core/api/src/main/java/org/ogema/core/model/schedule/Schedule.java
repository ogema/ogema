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
package org.ogema.core.model.schedule;

import org.ogema.core.model.Resource;
import org.ogema.core.timeseries.TimeSeries;

/**
 * A schedule contains data that is relevant for the future behavior of the resource to which it is attached. Depending
 * on the the actual type of the schedule, this may be a forecast of the value or a definition (e.g. a price profile, which is defined
 * by a source outside of the OGEMA gateway). <br>
 * 
 * Schedules can hold values of exactly one type, only (i.e. FloatValue or IntegerValue but not both). The type
 * of values a schedule accepts is defined by the type of the simple resource the schedule is attached to. <br>
 */
public interface Schedule extends Resource, TimeSeries {

}
