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
