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
package org.ogema.resourcemanager.impl.transaction;

import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.ResourceOperationException.Type;

public interface AtomicAction {
	
	/**
	 * Must be called only once.
	 * @throws Exception
	 * 		Will cause the rollback method of all actions
	 * 		of the transaction that have been executed hitherto
	 * 		to be triggered. 
	 */
	void execute() throws Exception;

	/**
	 * Must be called only once, after execute().
	 * @throws IllegalStateException
	 *  	if the action has not been executed yet
	 */
	void rollback() throws IllegalStateException;

	/**
	 * If false, only read-lock is requested for the operation.
	 * 
	 * As soon as a transaction contains a single operation that
	 * requires one of the locks, it will be acquired for 
	 * the duration of the entire transaction.  	
	 */
	boolean requiresStructureWriteLock();
	boolean requiresCommitWriteLock(); 
	
	/**
	 * Required for correct reporting of potential exceptions
	 * @return
	 */
	Type getType();
	
	/**
	 * Required for correct reporting of potential exceptions
	 * @return
	 */
	Resource getSource();
	
}
