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
package org.ogema.frameworkadministration.controller;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import org.ogema.core.administration.AdminLogger;
import org.ogema.core.administration.AdministrationManager;
import org.ogema.core.logging.LogLevel;
import org.ogema.core.logging.LogOutput;
import org.ogema.frameworkadministration.FrameworkAdministration;
import org.ogema.frameworkadministration.json.LoggerJsonSizeResponse;
import org.ogema.frameworkadministration.json.get.LoggerJsonGet;
import org.ogema.frameworkadministration.json.get.LoggerJsonGetList;
import org.ogema.frameworkadministration.json.post.LoggerJsonPost;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * @author tgries
 */
public class LoggerController {

	private AdministrationManager administrationManager;

	public static LoggerController instance;

	public static LoggerController getInstance() {
		if (instance == null) {
			instance = new LoggerController();
		}
		return instance;
	}

	private LoggerController() {
	}

	/**
	 * Reads the content of the OGEMA cache logger and returns it as a string
	 * @return the content of the cache as string 
	 */
	public String getCacheContent() {

		List<AdminLogger> loggers = administrationManager.getAllLoggers();
		String result = "{}";

		AdminLogger initLogger = loggers.get(0);
		List<String> list = initLogger.getCache();

		ObjectMapper mapper = new ObjectMapper();

		try {
			result = mapper.writeValueAsString(list);
		} catch (IOException ex) {
			Logger.getLogger(FrameworkAdministration.class.getName()).log(Level.SEVERE, null, ex);
		}

		return result;

	}

	/**
	 * Generates a list of all OGEMA loggers as json. See {@see org.ogema.frameworkadministration.json.get.LoggerJsonGetList} for message format.
	 * @param out write output to this stream.
	 */
	public void writeAllLoggersJSON(Writer out) {

		List<AdminLogger> loggers = administrationManager.getAllLoggers();

		if (loggers.isEmpty()) {
			return;
		}

		AdminLogger initLogger = loggers.get(0);

		String path = initLogger.getFilePath().getPath();
		long sizeFile = initLogger.getMaximumSize(LogOutput.FILE);
		long sizeCache = initLogger.getMaximumSize(LogOutput.CACHE);

		ObjectMapper mapper = new ObjectMapper();

		LoggerJsonGetList loggerList = new LoggerJsonGetList();
		loggerList.setPath(path);
		//loggerList.setSizeCache(sizeCache);
		loggerList.setSizeCache(new LoggerJsonSizeResponse("sizeCache", sizeCache, null, null));

		//loggerList.setSizeFile(sizeFile);
		loggerList.setSizeFile(new LoggerJsonSizeResponse("sizeFile", sizeFile, null, null));

		AdminLogger[] sortedLoggers = loggers.toArray(new AdminLogger[loggers.size()]);
		Arrays.sort(sortedLoggers, new Comparator<AdminLogger>() {

			@Override
			public int compare(AdminLogger o1, AdminLogger o2) {
				return o1.getName().compareTo(o2.getName());
			}

		});

		for (AdminLogger adminLogger : sortedLoggers) {

			String name = adminLogger.getName();
			LogLevel file = adminLogger.getMaximumLogLevel(LogOutput.FILE);
			LogLevel cache = adminLogger.getMaximumLogLevel(LogOutput.CACHE);
			LogLevel console = adminLogger.getMaximumLogLevel(LogOutput.CONSOLE);
			LoggerJsonGet loggerJson = new LoggerJsonGet(name, file, cache, console);
			loggerList.getList().add(loggerJson);
		}

		try {
			mapper.writeValue(out, loggerList);
		} catch (IOException ex) {
			Logger.getLogger(FrameworkAdministration.class.getName()).log(Level.SEVERE, null, ex);
		}

	}

	/**
	 * Sets the values for a given logger defined in the json message.
	 * @param loggerJsonPost json message. See {@see org.ogema.frameworkadministration.json.post.LoggerJsonPost} for message format.
	 */
	public void setLoggerValues(LoggerJsonPost loggerJsonPost) {

		String name = loggerJsonPost.getName();
		LogLevel file = loggerJsonPost.getFile();
		LogLevel cache = loggerJsonPost.getCache();
		LogLevel console = loggerJsonPost.getConsole();

		List<AdminLogger> loggers = administrationManager.getAllLoggers();
		for (AdminLogger adminLogger : loggers) {
			if (adminLogger.getName().equals(name)) {
				if (file != null) {
					adminLogger.overwriteMaximumLogLevel(LogOutput.FILE, file);
				}
				if (cache != null) {
					adminLogger.overwriteMaximumLogLevel(LogOutput.CACHE, cache);
				}
				if (console != null) {
					adminLogger.overwriteMaximumLogLevel(LogOutput.CONSOLE, console);
				}
			}
		}
	}

	/**
	 * Sets the size for a given logger. Sends a message to the servlet response on success or failure.
	 * @param logOutput OGEMA output target
	 * @param sizeBytes the size in bytes
	 * @param resp the HttpServletResponse object from the servlet class
	 */
	public void setSizeLogger(LogOutput logOutput, long sizeBytes, HttpServletResponse resp) {
		List<AdminLogger> loggers = administrationManager.getAllLoggers();
		AdminLogger initLogger = loggers.get(0);
		initLogger.setMaximumSize(logOutput, sizeBytes);

		//give response with current values
		LoggerJsonSizeResponse loggerJsonSize = new LoggerJsonSizeResponse();

		long actualSizeBytes = initLogger.getMaximumSize(logOutput);
		if (actualSizeBytes == sizeBytes) {
			loggerJsonSize.setMsgType("SUCCESS");
			loggerJsonSize.setMsg("ok");
		}
		else {
			loggerJsonSize.setMsgType("ERROR");
			loggerJsonSize.setMsg("could not set values due to framework constraints");
		}

		switch (logOutput) {
		case FILE:
			loggerJsonSize.setName("sizeFile");
			break;
		case CACHE:
			loggerJsonSize.setName("sizeCache");
			break;
		default:
			loggerJsonSize.setName("unknown");
			break;
		}

		loggerJsonSize.setValue(initLogger.getMaximumSize(logOutput));

		ObjectMapper mapper = new ObjectMapper();

		String result = "{}";

		try {
			result = mapper.writeValueAsString(loggerJsonSize);
		} catch (IOException ex) {
			ex.printStackTrace();
			Logger.getLogger(FrameworkAdministration.class.getName()).log(Level.SEVERE, null, ex);
		}

		try {
			resp.getWriter().write(result);
			resp.setStatus(200);
		} catch (IOException ex) {
			ex.printStackTrace();
			Logger.getLogger(LoggerController.class.getName()).log(Level.SEVERE, null, ex);
		}

	}

	public AdministrationManager getAdministrationManager() {
		return administrationManager;
	}

	public void setAdministrationManager(AdministrationManager administrationManager) {
		this.administrationManager = administrationManager;
	}

}
