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

import java.util.List;

/**
 * The default OGEMA logger, extending org.slf4j.Logger. OGEMA applications can create an OgemaLogger via the central
 * {@link LoggerFactory} instance. This class extends the slf4j logger by defining three different output destinations
 * via the {@link LogOutput}. Each application can set the maximum {@link LogLevel} written to the respective LogOutput
 * via {@link #setMaximumLogLevel(LogOutput, LogLevel)}, but the configuration of the outputs themselves is restricted
 * to OGEMA administrators using the {@link org.ogema.core.administration.AdminLogger} interface. Additionally, an
 * application can ask for the current {@link LogOutput#CACHE} to be written to a file, which can be used for in-depth
 * analysis of an error that occurred.
 * 
 * In the absence of administrator settings, OGEMA frameworks must provide sensible default settings for the
 * OgemaLoggers.
 */
public interface OgemaLogger extends org.slf4j.Logger {

	/**
	 * Requests to set the maximum log level for the respective {@link LogOutput}. Log messages with a higher level will
	 * not be written to the respective output.
	 * 
	 * Example: "setMaximumLogLevelForOutput(LogOutput.FILE, LogLevel.ERROR)" causes only errors to be written to the
	 * disk.
	 * 
	 * @param output
	 *            The output target to be configured.
	 * @param level
	 *            The maximum level that is still written to the output.
	 */
	public void setMaximumLogLevel(LogOutput output, LogLevel level);

	/**
	 * Gets the maximum {@link LogLevel} that is currently still written to the respective output. Note that the
	 * settings demanded by an application can be overwritten by a framework administrator via the
	 * {@link org.ogema.core.administration.AdminLogger} interface or be restricted by implementation limits.
	 * 
	 * @param output
	 *            Destination for which the log level is asked for.
	 * @return Maximum level still written to output.
	 */
	public LogLevel getMaximumLogLevel(LogOutput output);

	/**
	 * Demands the current cache to be persistently saved for future inpection. Typically, this will mean writing the
	 * cache to a file, but different ways of saving, e.g. sending an E-Mail to a central administration, are also
	 * possible. The OGEMA framework may deny the request.
	 * 
	 * @return true if the request was accepted, false if the framework rejected it.
	 */
	public boolean saveCache();

	/**
	 * Retrieve a copy of the current cache state.
	 * @return List of fully formatted log messages currently in the cache.
	 */
	public List<String> getCache();

}
