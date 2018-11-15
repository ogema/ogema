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
package org.ogema.core.application;

/**
 * Defines a listener that is informed whenever an exception is reported to the application. This includes framework-internal exceptions.
 */
public interface ExceptionListener {
	/**
	 * Notification to application on a framework exception.
	 * 
	 * @param exception
	 *            exception that occurred in the framework and that is caught there
	 */
	void exceptionOccured(Throwable exception);
}
