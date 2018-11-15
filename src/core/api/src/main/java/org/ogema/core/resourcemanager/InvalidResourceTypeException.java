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

/**
 * This Exception is thrown if a resource access is requested but the specified resource type doesn't match the type
 * of the accessed resource.
 * 
 */
public class InvalidResourceTypeException extends ResourceException {

	/**
	 * Creates an instance of the exception with an attached (error) message.
	 * @param msg message to attach to the exception.
	 */
	public InvalidResourceTypeException(String msg) {
		super(msg);
	}

	/**
	 * Creates an instance of the exception with an attached error message and
	 * an enclosed exception, that may have caused this exception.
	 */
	public InvalidResourceTypeException(String message, Throwable throwable) {
		super(message, throwable);
	}

	private static final long serialVersionUID = 8684009906944084062L;
}
