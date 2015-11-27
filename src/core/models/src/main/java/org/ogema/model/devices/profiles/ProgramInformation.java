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
	 * A program id
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
