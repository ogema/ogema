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
package org.ogema.core.resourcemanager;

/**
 * Exception thrown when an application tries to write a resource for which
 * it does not have the correct {@link AccessMode}. (If a write operation
 * is denied because of insufficient security permissions a SecurityException
 * will be raised.)
 *
 * @author jlapp
 */
public class ResourceAccessException extends ResourceException {
	private static final long serialVersionUID = 1L;

	public ResourceAccessException(String message) {
		super(message);
	}

}
