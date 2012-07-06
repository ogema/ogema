/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
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

/**
 * Possible log levels for logging with the {@link OgemaLogger}. The levels correspond to those defined in slf4j, but
 * are additionally given a ranking that discriminates between less important and more important levels. The order of
 * importance fron lowest to highest is TRACE - DEBUG - INFO - WARNING - ERROR. The special level
 * {@link LogLevel#NO_LOGGING} can be used in conjunction with
 * {@link OgemaLogger#setMaximumLogLevel(org.ogema.core.logging.LogOutput, org.ogema.core.logging.LogLevel) Logger.setMaximumLogLevel}
 * to indicate that no log level should be written to the respective output at all.
 */
public enum LogLevel {
	/**
	 * Message is logged to the temporary message cache and will only appear if
	 * explicity requested.
	 */
	TRACE,
	/**
	 * Message is relevant for debugging, but not for regular operation.
	 */
	DEBUG,
	/**
	 * General information reported. Resulting from proper behavior of the application/framework.
	 */
	INFO,
	/**
	 * Warning messages that may indicate an error, framework misbehavior or bad settings.
	 */
	WARNING,
	/**
	 * Message is the report of an error that occurred.
	 */
	ERROR,
	/**
	 * Use no logging at all. Can be used for configuring the logger.
	 */
	NO_LOGGING
}
