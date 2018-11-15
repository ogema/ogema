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
package org.ogema.core.model.array;

import org.ogema.core.resourcemanager.ResourceAccessException;
import org.ogema.core.resourcemanager.VirtualResourceException;

/**
 * Resource type representing an array of boolean values.
 */
public interface BooleanArrayResource extends ArrayResource {

	/**
	 * Gets all values.
	 * 
	 * @return a copy of the array of values represented by this resource. If no represented values exist, this returns an empty
	 *         array. This can never return null.
	 */
	boolean[] getValues();

	/**
	 * Replace the represented array with a new one.
	 * 
	 * @param values
	 *            new values for the represented array. If this is null, the call is ignored. This new array of values
	 *            may have a different size than the old one.
	 * @return returns true if the values could be written, false if not (e.g. if access mode is read-only).	 
	 */
	boolean setValues(boolean[] values);
	
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
	boolean[] getAndSet(boolean[] values) throws VirtualResourceException, SecurityException, ResourceAccessException;

	/**
	 * Gets the value of a single element in the array.
	 * 
	 * @param index
	 *            position of the element this request refers to (with index=0 referring to the first entry in the
	 *            array).
	 * @return returns the value at position index. If index is out of bounds of the array, a
	 *         java.lang.ArrayIndexOutOfBoundsException is thrown.
	 */
	boolean getElementValue(int index);

	/**
	 * Sets the value of a single element in the array.
	 * 
	 * @param value
	 *            new value to set the element's value to.
	 * @param index
	 *            position of the element this request refers to (with index=0 referring to the first entry in the
	 *            array).
	 */
	void setElementValue(boolean value, int index);

	/** 
	 * Returns the number of entries in the array. 
	 * @return
	 */
	int size();
}
