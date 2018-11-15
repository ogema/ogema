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
package org.ogema.core.logging;

import org.slf4j.ILoggerFactory;

/**
 * LoggerFactory defines a central instance from which all OGEMA applications can obtain a logger for text logging. The
 * recommended way to get a logger for an application is via {@link org.ogema.core.application.ApplicationManager#getLogger()}.
 * Since the OGEMA logger is a full SLF4J implementation, calling SLF4J's {@link org.slf4j.LoggerFactory#getLogger(java.lang.Class)}
 * will also return an OGEMA logger instance.
 * 
 * Logging of measurement data is taken care of by the {@link org.ogema.core.recordeddata.RecordedData}.
 */
public interface LoggerFactory extends ILoggerFactory {
	/**
	 * Generate an {@link OgemaLogger}, which is a logger with extended features over the default slf4j logging.
	 * 
	 * @param name
	 *            Name of the logger. If the parameter is null or empty, the default logger with the name "StdLog" is
	 *            returned.
	 * @return Returns an OgemaLogger.
	 */
	@Override
	OgemaLogger getLogger(String name);

	/**
	 * Generate an {@link OgemaLogger}, which is a logger with extended features over the default slf4j logging. The
	 * framework automatically determines the logger name from the class. Intended usage in a class using the logger:
	 * getOgemaLogger(this.getClass());.
	 * 
	 * @param clazz
	 *            Class for which to auto-create a logger.
	 * @return Returns an OgemaLogger with a name inferred from the Class parameter.
	 */
	OgemaLogger getLogger(Class<?> clazz);
}
