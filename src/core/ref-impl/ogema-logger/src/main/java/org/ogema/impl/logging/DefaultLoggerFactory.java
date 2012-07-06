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
package org.ogema.impl.logging;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.helpers.NOPAppender;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.RollingPolicy;
import ch.qos.logback.core.util.FileSize;
import ch.qos.logback.core.util.StatusPrinter;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.LoggingPermission;

import org.ogema.core.administration.AdminLogger;
import org.ogema.core.logging.LoggerFactory;
import org.ogema.core.logging.OgemaLogger;

/**
 * Logger factory of the OGEMA reference implementation. Creates {@link OgemaLogger}s.
 * 
 * @author jlapp
 */
public enum DefaultLoggerFactory implements LoggerFactory {

	// this singleton class is implemented as an enum.
	INSTANCE;
	public static final String DEFAULTLOGGERNAME = "StdLog";
    
    static final long CACHE_SIZE_LIMIT = FileSize.valueOf("100MB").getSize();
    static final long TOTAL_FILESIZE_LIMIT = FileSize.valueOf("2GB").getSize();
    static final long TOTAL_FILESIZE_MINIMUM = 0;
	// cache settings
	protected String dumpfile = "temp/cache_%d.log"; // default: use with
														// String.format(dumpfile,System.currentTimeMillis())
	final protected Map<String, DefaultLogger> loggersByName = new HashMap<>();
	final protected Appender<ILoggingEvent> fileOutput;
	final protected Appender<ILoggingEvent> cacheOutput;
	final protected Appender<ILoggingEvent> consoleOutput;
	final protected LoggerContext context;

	public static DefaultLoggerFactory getLoggerFactory() {
		return INSTANCE;
	}

	@SuppressWarnings({ "unchecked", "rawtypes", "UseOfSystemOutOrSystemErr" })
	private DefaultLoggerFactory() {
		String configMessage = "logging configured with logback defaults";

		context = new LoggerContext();
		URL defaultConfig = getClass().getResource("/logback.xml");
		String userConfig = System.getProperty("logback.configurationFile", "config/logback.xml");

		// read configuration from file or, if file doesn't exist, from bundle default configuration.
		try {
			if (new File(userConfig).exists()) {
				JoranConfigurator configurator = new JoranConfigurator();
				configurator.setContext(context);
				context.reset();
				configurator.doConfigure(userConfig);
				configMessage = "logging configured from file " + userConfig;
			}
			else {
				if (defaultConfig != null) {
					JoranConfigurator configurator = new JoranConfigurator();
					configurator.setContext(context);
					context.reset();
					configurator.doConfigure(defaultConfig);
					configMessage = "logging configured from OGEMA reference implementation bundle";
				}
				else {
					// work on logback default config, setup fallback appenders below
				}
			}
		} catch (JoranException je) {
			// StatusPrinter will handle this
		} finally {
			StatusPrinter.printInCaseOfErrorsOrWarnings(context);
		}

		Logger root = context.getLogger("ROOT");
		String error = "";

		if (root.getAppender("CONSOLE") != null) {
			consoleOutput = root.getAppender("CONSOLE");
			root.detachAppender(consoleOutput);
		}
		else {
			ConsoleAppender<ILoggingEvent> app = new ConsoleAppender<>();
			PatternLayout pl = new PatternLayout();
			pl.setPattern("%d{HH:mm:ss.SSS} %logger{36} [%thread] %-5level - %msg%n");
			pl.setContext(context);
			pl.start();
			app.setName("CONSOLE");
			app.setLayout(pl);
			app.setContext(context);
			app.start();
			root.addAppender(app);
			consoleOutput = app;
			error += "ERROR: broken logging configuration: CONSOLE appender not found\n";
		}

		if (root.getAppender("FILE") != null) {
			fileOutput = root.getAppender("FILE");
			root.detachAppender(fileOutput);
		}
		else {
			fileOutput = new NOPAppender<>();
			fileOutput.setName("FILE");
			fileOutput.setContext(context);
			fileOutput.start();
			root.addAppender(fileOutput);
			error += "ERROR: broken logging configuration: FILE appender not found\n";
		}

		if (root.getAppender("CACHE") != null) {
			cacheOutput = root.getAppender("CACHE");
			root.detachAppender(cacheOutput);
		}
		else {
			cacheOutput = new NOPAppender<>();
			cacheOutput.setName("CACHE");
			cacheOutput.setContext(context);
			cacheOutput.start();
			root.addAppender(cacheOutput);
			error += "ERROR: broken logging configuration: CACHE appender not found\n";
		}

		if (!error.isEmpty()) {
			context.getLogger(DEFAULTLOGGERNAME).error(error);
			System.err.println(error);
		}
		context.getLogger(DEFAULTLOGGERNAME).info(configMessage);
	}

	public List<AdminLogger> getAdminLoggers() {
		checkLoggingControlPermission();
		synchronized (loggersByName) {
			List<AdminLogger> rval = new ArrayList<>(loggersByName.size());
			for (DefaultLogger l : loggersByName.values()) {
				rval.add(new DefaultAdminLogger(l));
			}
			return rval;
		}
	}
    
    static void checkLoggingControlPermission(){
        if (System.getSecurityManager() != null){
            Permission loggingControlPermission = new LoggingPermission("control", null);
            System.getSecurityManager().checkPermission(loggingControlPermission);
        }
    }

	/**
	 * Directory containing log files, or null if file output does not work because of misconfiguration.
     * @return the log file directory
	 */
	public File getFilePath() {
		if (fileOutput instanceof RollingFileAppender) {
			RollingFileAppender<?> rfa = (RollingFileAppender) fileOutput;
			String activeFileName = rfa.getRollingPolicy().getActiveFileName();
			File activeFile = new File(activeFileName);
			return activeFile.getParentFile();
		}
		else {
			return null;
		}
	}

	/**
	 * Dump cache to file.
	 */
	synchronized void saveCache() throws IOException {
		if (cacheOutput instanceof CacheAppender) {
			((CacheAppender) cacheOutput).saveCache();
		}
		else {
			// running with a broken config - do nothing
		}
	}
    
    /**
	 * Dump cache to file.
	 */
    @SuppressWarnings("unchecked")
	synchronized List<String> getCache(){
		if (cacheOutput instanceof CacheAppender) {
			return CacheAppender.class.cast(cacheOutput).getCache();
		}
		else {
			return Collections.emptyList();
		}
	}

	protected void setCacheSize(long size) {
        checkLoggingControlPermission();
        if (size < 0 || size > CACHE_SIZE_LIMIT){
            return;
        }
		if (cacheOutput instanceof CacheAppender) {
			CacheAppender.class.cast(cacheOutput).setSize(Long.toString(size));
		}
		else {
			// running with a broken config - do nothing
		}
	}

	/**
	 * Maximum size of the log message cache, may be {@code -1} if the cache if unavailable due to misconfiguration.
     * @return cache size in bytes
	 */
	public long getCacheSize() {
		if (cacheOutput instanceof CacheAppender) {
			return ((CacheAppender) cacheOutput).getSizeLong();
		}
		else {
			return -1;
		}
	}

	protected void setLogfileSize(long size) {
        checkLoggingControlPermission();
		if (size <= TOTAL_FILESIZE_MINIMUM || size > TOTAL_FILESIZE_LIMIT) {
			return;
		}
		if (fileOutput instanceof RollingFileAppender) {
			RollingFileAppender<?> rfa = (RollingFileAppender) fileOutput;
			RollingPolicy rp = rfa.getRollingPolicy();
			if (rp instanceof HousekeepingPolicy) {
				((HousekeepingPolicy) rp).setMaxTotalSize(size);
			}
		}
		else {
			// running with a broken config - do nothing
		}
	}

	/**
	 * Maximum size of all log files, may be {@code -1} if file output if unavailable due to misconfiguration.
     * @return maximum total log file size
	 */
	public long getLogfileSize() {
		if (fileOutput instanceof RollingFileAppender) {
			RollingFileAppender<?> rfa = (RollingFileAppender) fileOutput;
			RollingPolicy rp = rfa.getRollingPolicy();
			if (rp instanceof HousekeepingPolicy) {
				return ((HousekeepingPolicy) rp).getMaxTotalSizeLong();
			}
		}
		return -1;
	}

	@Override
	public OgemaLogger getLogger(Class<?> clazz) {
		if (clazz == null) {
			return getLogger(DEFAULTLOGGERNAME);
		}
		else {
			return getLogger(clazz.getName());
		}
	}

	@Override
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
	public OgemaLogger getLogger(String name) {
		if (name == null || name.isEmpty()) {
			name = DEFAULTLOGGERNAME;
		}
		if (name.equalsIgnoreCase("ROOT")) {
			/*
			 * ensures that we only return the logback root logger once. strange logback behaviour: logger names are
			 * usually case sensitive, except "root"
			 */
			name = "ROOT";
		}
		DefaultLogger logger = loggersByName.get(name);
		if (logger == null) {
			Logger logbackLogger = context.getLogger(name);
			logger = new DefaultLogger(INSTANCE, logbackLogger);
			synchronized (loggersByName) {
				loggersByName.put(name, logger);
				if (!name.equals(logger.getName())) {
					System.err.printf("warning: logback logger name (%s) does not match requested name (%s)%n",
							logger.getName(), name);
				}
			}
		}
		return logger;
	}
}
