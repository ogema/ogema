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
