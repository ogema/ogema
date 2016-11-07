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
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.model.prototypes.Data;
import org.ogema.model.prototypes.PhysicalElement;

/**
 * State of a {@link PhysicalElement device}, such as "Off", "On", "Stand-by".
 */
public interface State extends Data {
	
	/**
	 * A unique (per device) id<br>
	 * Use subresource {@link Data#name() name} instead to specify a human-readable name for the state. 
	 */
	StringResource stateId();
	
	/**
	 * Keys:
	 * <ul>
	 * 	<li>0: Off state
	 *  <li>1: On state
	 *  <li>2: Stand-by 
	 *  <li>10000 and above: custom types. Note that custom values should only be used if 
	 *  	none of the above types applies.
	 * </ul>
	 *
	 */
	IntegerResource type();	
	
	/**
	 * Profiles for this state, each describing the time evolution of one physical quantity 
	 */
	ResourceList<Profile> profiles();
	
}
