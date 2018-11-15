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
