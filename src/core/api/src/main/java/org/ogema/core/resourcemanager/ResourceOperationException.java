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
