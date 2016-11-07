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
package org.ogema.core.channelmanager.driverspi;

/**
 * An Exception thrown if interface doesn't exist
 */
public class NoSuchInterfaceException extends Exception {

	private static final long serialVersionUID = -9152938891467324486L;

	private String interfaceId = null;

	/**
	 * Constructor
	 * 
	 * @param interfaceId
	 */
	public NoSuchInterfaceException(String interfaceId) {
		this.interfaceId = interfaceId;
	}

	/**
	 * Constructor 
	 * 
	 * @param interfaceId
	 * @param message
	 */
	public NoSuchInterfaceException(String interfaceId, String message) {
		super(message);
		this.interfaceId = interfaceId;
	}

	/**
	 * 
	 * @return interfaceId
	 */
	public String getInterfaceId() {
		return interfaceId;
	}
}
