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
