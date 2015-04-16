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
package org.ogema.core.channelmanager.measurements;

/**
 * A Exception thrown if a requested (implicit) conversion between different
 * types of values is not possible.
 */
public class IllegalConversionException extends RuntimeException {

	/**
	 * Create a new exception with a message attached to it.
	 * @param msg Message to attach to the exception.
	 */
	public IllegalConversionException(String msg) {
		super(msg);
	}

	private static final long serialVersionUID = 2L;

}
