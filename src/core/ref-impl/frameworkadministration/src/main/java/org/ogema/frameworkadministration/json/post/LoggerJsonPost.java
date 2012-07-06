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
package org.ogema.frameworkadministration.json.post;

import java.io.Serializable;
import org.ogema.core.logging.LogLevel;

/**
 *
 * @author tgries
 */
public class LoggerJsonPost implements Serializable {

	private static final long serialVersionUID = 5782741506890635527L;

	private String name;
	private LogLevel file;
	private LogLevel cache;
	private LogLevel console;
	private long value;

	public LoggerJsonPost(String name, LogLevel file, LogLevel cache, LogLevel console, long value) {
		this.name = name;
		this.file = file;
		this.cache = cache;
		this.console = console;
		this.value = value;
	}

	public LoggerJsonPost() {
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

	public long getValue() {
		return value;
	}

	public void setValue(long value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "LoggerJsonPost{" + "name=" + name + ", file=" + file + ", cache=" + cache + ", console=" + console
				+ ", value=" + value + '}';
	}

}
