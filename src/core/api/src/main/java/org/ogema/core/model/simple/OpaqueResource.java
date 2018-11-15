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
package org.ogema.core.model.simple;

import org.ogema.core.model.ValueResource;
import org.ogema.core.model.array.ArrayResource;
import org.ogema.core.model.array.ByteArrayResource;
import org.ogema.core.resourcemanager.ResourceAccessException;
import org.ogema.core.resourcemanager.VirtualResourceException;

/**
 * Simple resource holding a byte array that can represent any type of data (e.g. files).
 * @deprecated Use {@link ByteArrayResource}, instead.
 */
@Deprecated
public interface OpaqueResource extends org.ogema.core.model.SimpleResource, ValueResource, ArrayResource {
	/**
	 * Gets a copy of the data stored in the resource. If no values were set, an empty array
	 * is returned. This never returns null.
	 */
	byte[] getValue();

	/**
	 * Sets the resource data to a copy of the data passed
	 * @return returns true if the value could be written, false if not (e.g. if access mode is read-only).
	 */
	boolean setValue(byte[] value);
	
	/**
	 * Atomically sets to the given value and returns the previous value.
	 * 
	 * @param value
	 * 		the new value to be set
	 * @return
	 * 		the previous value
	 * @throws VirtualResourceException
	 * 		if the resource is virtual
	 * @throws SecurityException
	 * 		if the caller does not have the read and write permission for this resource
	 * @throws ResourceAccessException
	 * 		if access mode is read-only
	 */
	byte[] getAndSet(byte[] value) throws VirtualResourceException, SecurityException, ResourceAccessException;
}
