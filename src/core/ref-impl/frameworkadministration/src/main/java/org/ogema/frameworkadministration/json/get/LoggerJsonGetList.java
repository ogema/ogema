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
import java.util.ArrayList;
import java.util.List;
import org.ogema.frameworkadministration.json.LoggerJsonSizeResponse;

/**
 *
 * @author tgries
 */
public class LoggerJsonGetList implements Serializable {

	private static final long serialVersionUID = 6717290448515533216L;

	private List<LoggerJsonGet> loggers = new ArrayList<LoggerJsonGet>();

	private String path;
	//long sizeFile;
	//long sizeCache;
	private LoggerJsonSizeResponse sizeFile;
	private LoggerJsonSizeResponse sizeCache;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public LoggerJsonSizeResponse getSizeFile() {
		return sizeFile;
	}

	public void setSizeFile(LoggerJsonSizeResponse sizeFile) {
		this.sizeFile = sizeFile;
	}

	public LoggerJsonSizeResponse getSizeCache() {
		return sizeCache;
	}

	public void setSizeCache(LoggerJsonSizeResponse sizeCache) {
		this.sizeCache = sizeCache;
	}

	//	public long getSizeFile() {
	//		return sizeFile;
	//	}
	//
	//	public void setSizeFile(long sizeFile) {
	//		this.sizeFile = sizeFile;
	//	}
	//
	//	public long getSizeCache() {
	//		return sizeCache;
	//	}
	//
	//	public void setSizeCache(long sizeCache) {
	//		this.sizeCache = sizeCache;
	//	}

	public List<LoggerJsonGet> getList() {
		return loggers;
	}

	public void setList(List<LoggerJsonGet> list) {
		this.loggers = list;
	}

}
