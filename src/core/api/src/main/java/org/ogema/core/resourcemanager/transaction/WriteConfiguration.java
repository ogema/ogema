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
 * Specifies how a write operation in a 
 * {@link ResourceTransaction} deals with inactive and virtual resources.
 */
public enum WriteConfiguration {
	
	/**
	 * Write the resource value, even if the resource is inactive, but do not
	 * change its active status. Ignore virtual resources.
	 */
	IGNORE,
	
	/**
	 * Activate a resource if it exists but is inactive.
	 * Virtual resources are ignored.
	 */
	ACTIVATE,
	
	/**
	 * Create and activate a resource if it does not exist, respectively
	 * is inactive.
	 */
	CREATE_AND_ACTIVATE,
	
	/**
	 * Abort the transaction if the resource is inactive or virtual.
	 */
	FAIL

}
