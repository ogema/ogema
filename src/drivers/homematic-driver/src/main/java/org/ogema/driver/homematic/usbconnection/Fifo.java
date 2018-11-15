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
