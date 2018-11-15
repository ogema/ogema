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
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.model.prototypes.Data;

/**
 * A device program, characterised by a well-defined time profile of one or more measurable quantities, such as
 * a power consumption pattern. The subresource {@link ProgramInformation#profiles()} contains the actual profiles. 
 * They could describe for instance the power consumption of a device during one 
 * operating cycle (e.g. for household appliances like washing machines), or the characteristic time 
 * evolution of a set of quantities during the device start-up until they reach a stable value. 
 */
public interface ProgramInformation extends Data {

	/**
	 * A program id.<br> 
	 * Use {@link Data#name()} instead for a human readable, not necessarily unique, name.
	 */
	StringResource programId();

	/**
	 * Profiles belonging to this program, each describing the time evolution of one physical quantity 
	 */
	ResourceList<Profile> profiles();

	/**
	 * Average duration of the program; should coincide with the duration of the time profiles defined in
	 * {@link #profiles()}.
	 */
	TimeResource duration();

	/**
	 * Last start time of the program  
	 */
	TimeResource lastStartTime();

	/**
	 * Is the program currently active?
	 */
	BooleanResource isCurrentlyActive();
}
