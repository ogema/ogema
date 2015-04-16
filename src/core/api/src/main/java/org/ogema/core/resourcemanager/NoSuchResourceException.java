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
package org.ogema.core.resourcemanager;

/**
 * Exception indicating that an optional element referenced to in an addOptionalElement or setOptionalElement call does
 * not exist.
 */
public class NoSuchResourceException extends ResourceException {

	/**
	 * Creates an instance of the exception with an attached (error) message.
	 * @param message message to attach to the exception.
	 */
	public NoSuchResourceException(String message) {
		super(message);
	}

	/**
	 * Creates an instance of the exception with an attached error message and
	 * an enclosed exception, that may have caused this exception.
	 * @param message detail message
	 * @param t exception cause
	 */
	public NoSuchResourceException(String message, Throwable t) {
		super(message, t);
	}

	private static final long serialVersionUID = 8684009906944084063L;

}
