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
package org.ogema.core.channelmanager;

/**
 * ChannelAccessException is thrown by the ChannelAccess interface.
 * It may encapsulate any lower level exception as its cause.
 */
public class ChannelAccessException extends Exception {

	private static final long serialVersionUID = -3043173064927193401L;

	public ChannelAccessException(String message) {
		super(message);
	}

	public ChannelAccessException(Exception cause) {
		super(cause);
	}
	
	public ChannelAccessException(String message, Exception cause) {
		super(message, cause);
	}
}
