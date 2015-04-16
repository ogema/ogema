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
package org.ogema.frameworkadministration.json.get;

import java.io.Serializable;
import org.ogema.core.logging.LogLevel;

/**
 *
 * @author tgries
 */
public class LoggerJsonGet implements Serializable {

	private static final long serialVersionUID = 6208557310292945221L;

	private String name;
	private LogLevel file;
	private LogLevel cache;
	private LogLevel console;

	public LoggerJsonGet(String name, LogLevel file, LogLevel cache, LogLevel console) {
		this.name = name;
		this.file = file;
		this.cache = cache;
		this.console = console;
	}

	public LoggerJsonGet() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public LogLevel getFile() {
		return file;
	}

	public void setFile(LogLevel file) {
		this.file = file;
	}

	public LogLevel getCache() {
		return cache;
	}

	public void setCache(LogLevel cache) {
		this.cache = cache;
	}

	public LogLevel getConsole() {
		return console;
	}

	public void setConsole(LogLevel console) {
		this.console = console;
	}

}
