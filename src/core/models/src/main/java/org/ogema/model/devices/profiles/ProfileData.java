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
