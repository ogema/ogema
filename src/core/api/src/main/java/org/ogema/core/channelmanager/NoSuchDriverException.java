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
package org.ogema.core.channelmanager;

/**
 * Catch NoSuchDriverExceptions
 * 
 */
public class NoSuchDriverException extends Exception {

	private static final long serialVersionUID = -321028006466133825L;
	private final String driverID;

	/**
	 * save parameter at NoSuchDriverException Object
	 * 
	 * @param driverID
	 */
	public NoSuchDriverException(String driverID) {
		this.driverID = driverID;
	}

	/**
	 * @return Driver not installed: driverID
	 */
	@Override
	public String getMessage() {
		return "Driver not installed: " + driverID;
	}

}
