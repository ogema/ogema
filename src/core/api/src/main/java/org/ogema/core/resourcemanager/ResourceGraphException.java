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
