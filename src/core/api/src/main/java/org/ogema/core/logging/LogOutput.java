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
