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

/**
 * Resource type representing an array of time values.
 */
public interface TimeArrayResource extends ArrayResource {

	/**
	 * Gets all values.
	 * 
	 * @return a copy of the array of values represented by this resource. If no represented values exist, this returns an empty
	 *         array. This can never return null.
	 */
	long[] getValues();

	/**
	 * Replace the represented array with a new one.
	 * 
	 * @param values
	 *            new values for the represented array. If this is null, the call is ignored. This new array of values
	 *            may have a different size than the old one.
	 * @return returns true if the values could be written, false if not (e.g. if access mode is read-only).	 
	 */
	boolean setValues(long[] values);

	/**
	 * Gets the value of a single element in the array.
	 * 
	 * @param index
	 *            position of the element this request refers to (with index=0 referring to the first entry in the
	 *            array).
	 * @return returns the value at position index. If index is out of bounds of the array, a
	 *         java.lang.ArrayIndexOutOfBoundsException is thrown.
	 */
	long getElementValue(int index);

	/**
	 * Sets the value of a single element in the array.
	 * 
	 * @param value
	 *            new value to set the element's value to.
	 * @param index
	 *            position of the element this request refers to (with index=0 referring to the first entry in the
	 *            array).
	 */
	void setElementValue(long value, int index);

	/** Returns the number of entries in the array. */
	int size();
}
