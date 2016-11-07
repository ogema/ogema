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
