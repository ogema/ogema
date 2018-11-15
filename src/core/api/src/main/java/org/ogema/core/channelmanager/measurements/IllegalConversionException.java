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
package org.ogema.core.channelmanager.measurements;

/**
 * An Exception thrown if a requested (implicit) conversion between different
 * types of values is not possible.
 */
public class IllegalConversionException extends RuntimeException {

	/**
	 * Create a new exception with a message attached to it.
	 * @param msg Message to attach to the exception.
	 */
	public IllegalConversionException(String msg) {
		super(msg);
	}

	private static final long serialVersionUID = 2L;

}
