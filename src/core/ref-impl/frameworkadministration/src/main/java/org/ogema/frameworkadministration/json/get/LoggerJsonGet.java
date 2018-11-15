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
