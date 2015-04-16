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
