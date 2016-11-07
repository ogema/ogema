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

import java.util.Objects;

import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.transaction.ResourceTransaction;

/**
 * Wraps an exception that is thrown during the execution of a {@link ResourceTransaction}. 
 * The actual exception is accessible via {@link Exception#getCause()}, which never returns null for 
 * this exception type. The type of operation that failed is reported via {@link #getOperationType()},
 * and the target resource of the operation via {@link #getSource()}. 
 */
public abstract class ResourceOperationException extends ResourceException {

	private static final long serialVersionUID = 7908730304677972899L;

	public ResourceOperationException(String message, Throwable t) {
		super(message, t);
		Objects.requireNonNull(t);
	}

	public static enum Type {
		
		CREATE, DELETE, REFERENCE, ACTIVATE, DEACTIVATE, WRITE, READ, ACCESS_MODE
		
	}
	
	public abstract Type getOperationType();
	
	public abstract Resource getSource();
	
}
