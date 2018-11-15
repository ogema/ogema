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
package org.ogema.driver.iec62056p21;

public class ConnectionException extends Exception {

	private static final long serialVersionUID = -6482447005742984400L;

	public ConnectionException() {
		super();
	}

	public ConnectionException(String s) {
		super(s);
	}

	public ConnectionException(Throwable cause) {
		super(cause);
	}

	public ConnectionException(String s, Throwable cause) {
		super(s, cause);
	}
}
