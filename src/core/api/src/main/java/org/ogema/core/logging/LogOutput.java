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

/**
 * Possible output targets for a log message. The association between a {@link LogLevel} and the output is configured in
 * {@link OgemaLogger}.
 */
public enum LogOutput {
	/** Write to the file associated with the logger. */
	FILE,
	/**
	 * Store the log message in an intermediate cache that is shared amongst all loggers. Messages in the cache will be
	 * overwritten with newer messages after some time. The current state of the cache can be written to disk via
	 * {@link OgemaLogger#saveCache()}, e.g. to provide background information in case of an error that was
	 * encountered.
	 */
	CACHE,
	/** Output the log message to the console. */
	CONSOLE
}
