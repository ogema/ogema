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
