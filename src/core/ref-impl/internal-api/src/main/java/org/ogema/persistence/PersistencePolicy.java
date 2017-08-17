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
package org.ogema.persistence;

/**
 * The resource database manages dynamic data which is the representation of the resources in the ogema system. The
 * dynamic data is stored persistently so the resource pool can be recreated out of the stored data. The storage can be
 * done in some different ways dependent on the abilities of the underlying physical system and the needs of the
 * application. These aspects can be respected by the implementation of this interface which provides the storage
 * support for the database implementation.
 * 
 */
public interface PersistencePolicy {

	/**
	 * The dynamic resource data can change in some different ways. This enumeration encodes the change of a node in the
	 * resource tree.
	 * 
	 */
	public enum ChangeInfo {
		NEW_RESOURCE, VALUE_CHANGED, STATUS_CHANGED, NEW_SUBRESOURCE, DELETED
	}

	/**
	 * Send a message to the storage sub system which node made which change.
	 * 
	 * @param resID
	 *            Unique index of the node in the database.
	 * @param changeInfo
	 *            One of the predefined values in {@link ChangeInfo} that specify the what exactly changed.
	 */
	public void store(int resID, ChangeInfo changeInfo);

	/**
	 * Signal the storage sub system of the database that a transaction is started. The {@link PersistencePolicy}
	 * implementation should synchronize the storage with the transactions which are initiated by the resource
	 * management to achieve a consistent persistent data at any time.
	 * 
	 * @param toplevelRes
	 *            Unique database index of the top level node which is transferred in the current transaction into the
	 *            database.
	 */
	public void startTransaction(int toplevelRes);

	/**
	 * Signal the storage sub system of the database that a transaction its start was signaled before, is now
	 * terminated.
	 * 
	 * @param toplevelRes
	 *            Unique database index of the top level node which its transaction into the database is finished.
	 */
	public void finishTransaction(int toplevelRes);

	/**
	 * Start the storage process. This method is called by the initialization of the database. Typically this method is
	 * called at the initialization time and only one time.
	 */
	public void startStorage();

	/**
	 * Stop the storage process. After the execution of this method no storage is proceeded and all changes in the
	 * dynamic data are ignored.
	 */
	public void stopStorage();

	/**
	 * Gets a lock object which is used to synchronize the storage thread with the resource data base.
	 * 
	 * @return The lock object
	 */
	public Object getStorageLock();

	/**
	 * Initiate an immediate storage loop asynchronously.
	 */
	public void triggerStorage();
}
