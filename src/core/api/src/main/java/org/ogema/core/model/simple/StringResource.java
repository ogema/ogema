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
