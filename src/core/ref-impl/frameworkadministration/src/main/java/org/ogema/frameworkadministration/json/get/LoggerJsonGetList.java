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
