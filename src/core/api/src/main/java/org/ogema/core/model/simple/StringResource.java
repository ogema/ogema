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
package org.ogema.core.model.simple;

import org.ogema.core.model.schedule.AbsoluteSchedule;
import org.ogema.core.resourcemanager.ResourceAccessException;
import org.ogema.core.resourcemanager.VirtualResourceException;

/**
 * Resource containing a single string entry.
 */
public interface StringResource extends SingleValueResource {

	/**
	 * Gets a copy of the text stored in the resource.
	 */
	String getValue();

	/**
	 * Sets a new text in the resource that equals a copy of the parameter
	 * passed.
	 * @return returns true if the value could be written, false if not (e.g. if access mode is read-only).* 
	 */
	boolean setValue(String value);
	
	/**
	 * Atomically sets to the given value and returns the previous value.
	 * 
	 * @param value
	 * 		the new value to be set
	 * @return
	 * 		the previous value
	 * @throws VirtualResourceException
	 * 		if the resource is virtual
	 * @throws SecurityException
	 * 		if the caller does not have the read and write permission for this resource
	 * @throws ResourceAccessException
	 * 		if access mode is read-only
	 */
	String getAndSet(String value) throws VirtualResourceException, SecurityException, ResourceAccessException;

	/**
	 * Future prognosis for this value. The data type, unit and interpretation of
	 * the values in the schedule are the same as the value in this. If multiple
	 * forecasts are available for some reason, this shall reflect the best-guess
	 * (either the best individual forecast or the best combined forecast). The
	 * other forecasts can be added as decorators.
	 */
	AbsoluteSchedule forecast();

	/**
	 * Future behavior of this value that shall be reached through management
	 * operations. 
	 * The data type, unit and interpretation of
	 * the values in the schedule are the same as the value in this.
	 */
	AbsoluteSchedule program();

}
