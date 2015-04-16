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
 * This Exception is thrown if a resource access should be done but the specified resource type doesn't match the type
 * of the accessed rersource.
 * 
 */
public class InvalidResourceTypeException extends ResourceException {

	/**
	 * Creates an instance of the exception with an attached (error) message.
	 * @param msg message to attach to the exception.
	 */
	public InvalidResourceTypeException(String msg) {
		super(msg);
	}

	/**
	 * Creates an instance of the exception with an attached error message and
	 * an enclosed exception, that may have caused this exception.
	 */
	public InvalidResourceTypeException(String message, Throwable throwable) {
		super(message, throwable);
	}

	private static final long serialVersionUID = 8684009906944084062L;
}
