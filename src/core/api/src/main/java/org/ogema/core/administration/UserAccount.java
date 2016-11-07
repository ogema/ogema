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
package org.ogema.core.administration;

/**
 * Access to the data of a user registered on the system.
 */
public interface UserAccount {

	/**
	 * Get user's name.
	 *
	 * @return the name
	 */
	public String getName();

	/**
	 * Change the password of the user associated with this account object.
	 *
	 * @param oldPassword
	 *            the old password
	 * @param newPassword
	 *            the new password
	 */
	public void setNewPassword(String oldPassword, String newPassword);

	/**
	 * Gets the market place user name which is associated with this account.
	 *
	 * @param storeName
	 *            the name of the marketplace.
	 * @return the user name which is registered on the market place for the user of this account.
	 */
	public String getStoreUserName(String storeName);
}
