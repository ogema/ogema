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
package org.ogema.core.administration;

import java.io.File;

import org.ogema.core.logging.LogLevel;
import org.ogema.core.logging.LogOutput;
import org.ogema.core.logging.OgemaLogger;

/**
 * Administration interface of the {@link org.ogema.core.logging.OgemaLogger}, allowing to overwrite framework default
 * settings and settings made by the applications.
 * 
 * The OGEMA API does not fully define the effects of all this interface's methods: The AdminLogger interface is only
 * available to framework administrators, who are expected to be implementation-aware.
 */
public interface AdminLogger extends OgemaLogger {

	/**
	 * Overwrite application settings with administration settings. If the {@link org.ogema.core.logging.LogLevel} is null,
	 * possibly existing override settings will be removed.
	 * 
	 * @param output
	 *            logger's output to affect.
	 * @param level
	 *            overwrite level. "null" to remove overwrite settings.
	 */
	public void overwriteMaximumLogLevel(LogOutput output, LogLevel level);

	/**
	 * Set the maximum size (in bytes) that a {@link org.ogema.core.logging.LogOutput} may use. The limit given applies to
	 * all Loggers' total output. This method only applies to output to {@link LogOutput#FILE} and
	 * {@link LogOutput#CACHE}; requests to set the maximum size for the console output can be ignored. <br>
	 * Depending on the implementation, the framework may reject or modify the request, e.g. by restricting sizes within
	 * a limiting window.
	 * 
	 * @param output
	 *            LogOutput for which the setting is made.
	 * @param bytes
	 *            new maximum size in bytes.
	 */
	public void setMaximumSize(LogOutput output, long bytes);

	/**
	 * Get the maximum size for the {@link LogOutput} destination.
	 * 
	 * @param output
	 *            Output destination for which the maximum size is asked for.
	 * @return The maximum number of bytes that may be used for all Loggers' total output.
	 */
	public long getMaximumSize(LogOutput output);

	/**
	 * Get the current path where log files are being written to.
	 * 
	 * @return Path the log files are being written to, either as relative or as absolute path.
	 */
	public File getFilePath();

}
