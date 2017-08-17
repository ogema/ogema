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
package org.ogema.core.model.array;

import org.ogema.core.resourcemanager.ResourceAccessException;
import org.ogema.core.resourcemanager.VirtualResourceException;

/**
 * Resource type representing an array of integer values.
 */
public interface IntegerArrayResource extends ArrayResource {

	/**
	 * Gets all values.
	 * 
	 * @return a copy of the array of values represented by this resource. If no represented values exist, this returns an empty
	 *         array. This can never return null.
	 */
	int[] getValues();

	/**
	 * Replace the represented array with a new one.
	 * 
	 * @param values
	 *            new values for the represented array. If this is null, the call is ignored. This new array of values
	 *            may have a different size than the old one.
	 * @return returns true if the values could be written, false if not (e.g. if access mode is read-only).	 	 
	 */
	boolean setValues(int[] values);
	
	/**
	 * Atomically sets to the given values and returns the previous values.
	 * 
	 * @param values
	 * 		the new values to be set
	 * @return
	 * 		the previous values
	 * @throws VirtualResourceException
	 * 		if the resource is virtual
	 * @throws SecurityException
	 * 		if the caller does not have the read and write permission for this resource
	 * @throws ResourceAccessException 
	 * 		if access mode is read-only
	 */
	int[] getAndSet(int[] values) throws VirtualResourceException, SecurityException, ResourceAccessException;

	/**
	 * Gets the value of a single element in the array.
	 * 
	 * @param index
	 *            position of the element this request refers to (with index=0 referring to the first entry in the
	 *            array).
	 * @return returns the value at position index. If index is out of bounds of the array, a
	 *         java.lang.ArrayIndexOutOfBoundsException is thrown.
	 */
	int getElementValue(int index);

	/**
	 * Sets the value of a single element in the array.
	 * 
	 * @param value
	 *            new value to set the element's value to.
	 * @param index
	 *            position of the element this request refers to (with index=0 referring to the first entry in the
	 *            array).
	 */
	void setElementValue(int value, int index);

	/** Returns the number of entries in the array. */
	int size();
}
