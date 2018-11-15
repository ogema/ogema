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
package org.ogema.core.channelmanager.driverspi;

/**
 * An Exception thrown if interface doesn't exist
 */
public class NoSuchInterfaceException extends Exception {

	private static final long serialVersionUID = -9152938891467324486L;

	private String interfaceId = null;

	/**
	 * Constructor
	 * 
	 * @param interfaceId
	 */
	public NoSuchInterfaceException(String interfaceId) {
		this.interfaceId = interfaceId;
	}

	/**
	 * Constructor 
	 * 
	 * @param interfaceId
	 * @param message
	 */
	public NoSuchInterfaceException(String interfaceId, String message) {
		super(message);
		this.interfaceId = interfaceId;
	}

	/**
	 * 
	 * @return interfaceId
	 */
	public String getInterfaceId() {
		return interfaceId;
	}
}
