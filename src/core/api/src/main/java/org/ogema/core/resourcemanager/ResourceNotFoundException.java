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
 * This exception is thrown if no resource can be found with the given id or name.
 * 
 */
public class ResourceNotFoundException extends ResourceException {

	public ResourceNotFoundException(String str) {
		super(str);
	}

	public ResourceNotFoundException(String message, Throwable throwable) {
		super(message, throwable);
	}

	private static final long serialVersionUID = 116240692864752437L;

}
