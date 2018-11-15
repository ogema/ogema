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
