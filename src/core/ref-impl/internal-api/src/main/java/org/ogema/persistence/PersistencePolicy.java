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
