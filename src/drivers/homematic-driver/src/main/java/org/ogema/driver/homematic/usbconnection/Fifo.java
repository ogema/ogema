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
package org.ogema.driver.homematic.usbconnection;

import org.slf4j.Logger;

/**
 * Fifo is a first in first out mechanisms standard construction with putptr and getptr the module is done by a mask
 * 
 * @param <T>
 */
public class Fifo<T> {
	Object[] entries;
	int putptr;
	int getptr;
	int count;
	int size;
	int mask;

	private final Logger logger = org.slf4j.LoggerFactory.getLogger("homematic-driver");

	/**
	 * Constructor
	 * 
	 * @param twosLogarithmOfSize
	 */
	public Fifo(int twosLogarithmOfSize) {
		if (twosLogarithmOfSize > 12)
			twosLogarithmOfSize = 12;
		putptr = 0;
		getptr = 0;
		count = 0;
		this.size = 1 << twosLogarithmOfSize; // 2 exponent
		mask = size - 1;
		entries = new Object[size];

	}

	/**
	 * put an Object - throws IndexOutOfBoundsException if full
	 * 
	 * @param o
	 */
	public synchronized void put(T o) {
		try {
			entries[putptr] = o;
			putptr++;
			putptr &= mask;
			count++;
		} catch (IndexOutOfBoundsException ioobe) {
			logger.warn("homematic-driver lost message because input fifo is full!");
		}
	}

	/**
	 * @return an Object or null if empty
	 */
	@SuppressWarnings("unchecked")
	public synchronized T get() {
		Object o = null;
		if (count > 0) {
			o = entries[getptr];
			entries[getptr] = null;
			getptr++;
			getptr &= mask;
			count--;
		}
		return (T) o;
	}

	/**
	 * clear the Fifo
	 */
	public synchronized void clear() {
		count = 0;
		putptr = 0;
		getptr = 0;
		entries = new Object[size];
	}

}
