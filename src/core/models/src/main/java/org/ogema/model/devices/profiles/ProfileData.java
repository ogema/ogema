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
package org.ogema.model.devices.profiles;

import org.ogema.core.model.ResourceList;
import org.ogema.core.model.schedule.RelativeSchedule;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.model.prototypes.Data;

/**
 * Repeatable time profile of some physical quantity, for instance the consumption profile of an electrical
 * device in a specific operating mode.<br> 
 * Typically, ProfileData is part of a {@link Profile}, where the physical quantity represented by the time series
 * is specified, see {@link Profile#sensor()}. <br>
 * Metadata can be added as decorators or defined in custom extensions of this model.
 */
public interface ProfileData extends Data {

	/**
	 * The actual time series this refers to. Must be a reference to a schedule below a 
	 * {@link SingleValueResource}. 
	 */
	RelativeSchedule profile();
	
	/**
	 * Specifies how the data was obtained, either from an actual measurement, or derived in some way.
	 * <ul>
	 * 	<li>0: actual measurement data
	 * 	<li>1: average curve from multiple measurements
	 * 	<li>2: idealized curve not directly obtained from measurements
	 * 	<li>10000 and above: custom types
	 * </ul>
	 */
	IntegerResource type();

	/**
	 * In case this schedule has been derived from multiple measurements that are available as schedules 
	 * in the system as well, references to the constituents can be given here. If this profile data represents
	 * a single measurement, this subresource should not be created. 
	 */
	ResourceList<RelativeSchedule> inputData();
	
}
