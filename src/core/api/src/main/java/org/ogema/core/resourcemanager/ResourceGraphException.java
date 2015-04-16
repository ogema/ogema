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
 * A ResourceGraphException is thrown as a result of an operation that by itself
 * is legal (i.e. can be performed) but would result in an illegal state of the
 * resources in the OGEMA system.<br>
 * For example, adding a reference to a valid target resource with the correct
 * type is a legal operation by itself, but may cause loops in which a resource
 * is its own sub-resource. Hence, the operation would not be performed and this
 * would be thrown.
 */
public class ResourceGraphException extends RuntimeException {

	private static final long serialVersionUID = 116240692864752438L;

	public ResourceGraphException(String message) {
		super(message);
	}

	public ResourceGraphException(String message, Throwable t) {
		super(message, t);
	}
}
