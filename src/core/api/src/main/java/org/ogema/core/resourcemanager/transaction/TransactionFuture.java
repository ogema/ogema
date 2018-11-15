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
package org.ogema.core.resourcemanager.transaction;

/**
 * Typically, each object of this type is associated to a resource, and the {@link #getValue()}
 * method returns the resource value read in a transaction. If getValue() is called before
 * the transaction has been executed, an exception is thrown. 
 * 
 * @param <T> the value type of the resource. For instance, @see java.lang.Float for 
 * {@link org.ogema.core.model.simple.FloatResource FloatResources}.
 */
public interface TransactionFuture<T> {

	/**
	 * Get the resource value associated to a read operation performed in a 
	 * {@link ResourceTransaction}.<br>
	 * The operation is non-blocking, but it must only be called after the transaction has been committed 
	 * (see {@link ResourceTransaction#commit()}). 
	 * @return
	 * 		The resource value, or null, if the resource did not exist or was inactive, and the transaction
	 * 		configuration demands to ignore such resources (see {@link ReadConfiguration}).
	 * @throws IllegalStateException
	 * 		If the associated transaction has not been executed yet, or any of the operations involved in 
	 * 		the transaction has thrown an exception, hence causing a rollback. 
	 */
	T getValue() throws IllegalStateException; 
	
}
