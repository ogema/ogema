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
