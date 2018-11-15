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
