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
package org.ogema.impl.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.filter.Filter;
import java.io.File;
import java.io.FileInputStream;

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.ogema.core.logging.LogLevel;
import org.ogema.core.logging.LogOutput;
import org.ogema.core.logging.OgemaLogger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

/**
 * Reference implementation of the OgemaLogger.
 *
 * @author jlapp
 */
@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class DefaultLogger implements OgemaLogger {

    protected final Logger logger;
    protected final DefaultLoggerFactory factory;

    protected final Map<LogOutput, OgemaFilter> filters = new EnumMap<>(LogOutput.class);
    private final List<OgemaFilter> addOnFilters;

    /**
     * System property ({@value}) for setting the path to the default log level
     * configuration, default value is {@value #LOGLEVELS_PROPERTIES_DEFAULT}.
     */
    public static final String LOGLEVELS_PROPERTIES = "ogema.logger.levels";
    public static final String LOGLEVELS_PROPERTIES_DEFAULT = "config/loglevels.properties";
    static final Properties loglevels = new Properties();

    static {
    	AccessController.doPrivileged(new PrivilegedAction<Void>() {

			@Override
			public Void run() {
				final String loglevelsFile = System.getProperty(LOGLEVELS_PROPERTIES, LOGLEVELS_PROPERTIES_DEFAULT);
				final File propFile = new File(loglevelsFile);
		        if (propFile.exists()) {
		            try (FileInputStream is = new FileInputStream(propFile)) {
		                loglevels.load(is);
		            } catch (IOException ioex) {
		                System.err.println(ioex);
		            }
		        }
		        return null;
			}
		});
    	
    }

    /**
     * Create an Ogema logger by wrapping a logback logger and adding custom
     * output filters.
     *
     * @param factory Factory that created this logger.
     * @param logger The logback delegate logger.
     */
    protected DefaultLogger(DefaultLoggerFactory factory, Logger logger) {
        this.logger = logger;
        this.factory = factory;

        logger.setAdditive(false);
        // we do not log packagingData and it requires RuntimePermission "getClassLoader"
        logger.getLoggerContext().setPackagingDataEnabled(false);

        OgemaFilter cacheFilter = new OgemaFilter();
        OgemaFilter fileFilter = new OgemaFilter();
        OgemaFilter consoleFilter = new OgemaFilter();
        // TODO add filters for 

        cacheFilter.setLevelUser(LogLevel.TRACE);
        fileFilter.setLevelUser(LogLevel.INFO);
        consoleFilter.setLevelUser(LogLevel.INFO);

        filters.put(LogOutput.CACHE, cacheFilter);
        filters.put(LogOutput.FILE, fileFilter);
        filters.put(LogOutput.CONSOLE, consoleFilter);

        if (factory.addOnAppenders != null) {
        	addOnFilters = new ArrayList<>();
        	for (int i=0;i<factory.addOnAppenders.size();i++) {
        		addOnFilters.add(new OgemaFilter());
        	}
        } else
        	addOnFilters = null;
        
        String loggerName = logger.getName();
        for (String configuredLevel : loglevels.stringPropertyNames()) {
            if (configuredLevel.endsWith("*")) {
                if (loggerName.startsWith(configuredLevel.substring(0, configuredLevel.length() - 1))) {
                    configureLevels(loglevels.getProperty(configuredLevel));
                }
                //evaluate the rest to allow complete matches to override wildcards
            } else if (loggerName.equals(configuredLevel)) {
                configureLevels(loglevels.getProperty(configuredLevel));
                break;
            }
        }

        logger.addAppender(createAppenderDecorator(factory.cacheOutput, cacheFilter));
        logger.addAppender(createAppenderDecorator(factory.fileOutput, fileFilter));
        logger.addAppender(createAppenderDecorator(factory.consoleOutput, consoleFilter));
        if (factory.addOnAppenders != null) {
        	for (int i=0;i<factory.addOnAppenders.size();i++) {
        		Appender<ILoggingEvent> appender = factory.addOnAppenders.get(i);
        		OgemaFilter filter = addOnFilters.get(i);
        		logger.addAppender(createAppenderDecorator(appender, filter));
        	}
        }
        
        setSlf4jLoggerLevel();
    }

    private void configureLevels(String levels) {
        String[] a = levels.split(",");
        if (a.length > 0) {
            configureLevel(a[0], LogOutput.CONSOLE);
        }
        if (a.length > 1) {
            configureLevel(a[1], LogOutput.FILE);
        }
        if (a.length > 2) {
            configureLevel(a[2], LogOutput.CACHE);
        }
        if (factory.addOnAppenders == null)
        	return;
        for (int i=3;i<Math.min(a.length,factory.addOnAppenders.size()+3);i++) {
        	final String level = a[i].trim().toUpperCase();
        	if (level.isEmpty())
        		continue;
        	try {
        		addOnFilters.get(i-3).setLevelUser(LogLevel.valueOf(level));
        	} catch (IllegalArgumentException iae) {
                String error = String.format("illegal log level value for logger %s, output %s: %s%n", 
                		logger.getName(), factory.addOnAppenders.get(i-3).getName(), level);
                LoggerFactory.getLogger("ROOT").error(error, iae);
            }
        }
        
        // TODO add custom appender log levels
    }

    private void configureLevel(String level, LogOutput output) {
        level = level.trim().toUpperCase();
        if (level.isEmpty()) {
            return;
        }
        try {
            filters.get(output).setLevelUser(LogLevel.valueOf(level));
        } catch (IllegalArgumentException iae) {
            String error = String.format("illegal log level value for logger %s, output %s: %s%n", logger.getName(), output, level);
            LoggerFactory.getLogger("ROOT").error(error, iae);

        }
    }
    
    private LogLevel getMaximumLevel() {
        LogLevel maxCache = getMaximumLogLevel(LogOutput.CACHE);
        LogLevel maxFile = getMaximumLogLevel(LogOutput.FILE);
        LogLevel maxConsole = getMaximumLogLevel(LogOutput.CONSOLE);
        LogLevel max = maxCache;
        if (maxFile.ordinal() < max.ordinal()){
            max = maxFile;
        }
        if (maxConsole.ordinal() < max.ordinal()){
            max = maxConsole;
        }
        if (addOnFilters != null) {
        	for (OgemaFilter filter : addOnFilters) {
        		if (filter.effectiveLevel.ordinal() < max.ordinal())
        			max = filter.effectiveLevel;
        	}
        }
        return max;
    }
    
    private static Level toSlf4jLevel(LogLevel lvl) {
        switch (lvl) {
            case DEBUG : return Level.DEBUG;
            case ERROR : return Level.ERROR;
            case INFO : return Level.INFO;
            case TRACE : return Level.TRACE;
            case WARNING : return Level.WARN;
            case NO_LOGGING : return Level.OFF;
            default : return Level.ALL;
        }
    }
    
    /* setting log level in logback requires (java.util.logging.LoggingPermission "control") */
    private static void setLogLevelPrivileged(final Logger logger, final Level level) {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {

            @Override
            public Void run() {
                logger.setLevel(level);
                return null;
            }
        });
    }
    
    private void setSlf4jLoggerLevel() {
        setLogLevelPrivileged(logger, toSlf4jLevel(getMaximumLevel()));
    }

    /**
     * Creates a new appender that filters according to its own criteria and
     * then forwards the message to another appender (which does not filter).
     *
     * @param appender other appender that the filtered message is being passed
     * to.
     *
     * @param filter Filter criteria for this.
     *
     * @return
     */
    static Appender<ILoggingEvent> createAppenderDecorator(final Appender<ILoggingEvent> appender,
            final Filter<ILoggingEvent> filter) {
        Appender<ILoggingEvent> app = new FilterAppender(appender, filter);
        app.start();
        return app;
    }

    @Override
    public LogLevel getMaximumLogLevel(LogOutput output) {
        return filters.get(output).effectiveLevel;
    }

    @Override
    public void setMaximumLogLevel(LogOutput output, LogLevel level) {
        if (output == null) {
            throw new IllegalArgumentException("output must not be null");
        }
        if (level == null) {
            throw new IllegalArgumentException("level must not be null");
        }
        filters.get(output).setLevelUser(level);
        setSlf4jLoggerLevel();
    }

    protected void overrideLogLevel(LogOutput output, LogLevel level) {
        DefaultLoggerFactory.checkLoggingControlPermission();
        if (level == null) {
            filters.get(output).unsetAdminLevel();
        } else {
            filters.get(output).setLevelAdmin(level);
        }
        setSlf4jLoggerLevel();
    }

    @Override
    public boolean saveCache() {
        try {
            factory.saveCache();
            return true;
        } catch (IOException ioex) {
            error("error writing cache", ioex);
            return false;
        }

    }

    @Override
    public List<String> getCache() {
        return factory.getCache();
    }

    // only logger delegate methods below this point -------------------------->
    @Override
    public String getName() {
        return logger.getName();
    }

    @Override
    public void trace(String msg) {
        logger.trace(msg);
    }

    @Override
    public void trace(String format, Object arg) {
        logger.trace(format, arg);
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        logger.trace(format, arg1, arg2);
    }

    @Override
    public void trace(String format, Object... argArray) {
        logger.trace(format, argArray);
    }

    @Override
    public void trace(String msg, Throwable t) {
        logger.trace(msg, t);
    }

    @Override
    public void trace(Marker marker, String msg) {
        logger.trace(marker, msg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg) {
        logger.trace(marker, format, arg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        logger.trace(marker, format, arg1, arg2);
    }

    @Override
    public void trace(Marker marker, String format, Object... argArray) {
        logger.trace(marker, format, argArray);
    }

    @Override
    public void trace(Marker marker, String msg, Throwable t) {
        logger.trace(marker, msg, t);
    }

    @Override
    public void debug(String msg) {
        logger.debug(msg);
    }

    @Override
    public void debug(String format, Object arg) {
        logger.debug(format, arg);
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        logger.debug(format, arg1, arg2);
    }

    @Override
    public void debug(String format, Object... argArray) {
        logger.debug(format, argArray);
    }

    @Override
    public void debug(String msg, Throwable t) {
        logger.debug(msg, t);
    }

    @Override
    public void debug(Marker marker, String msg) {
        logger.debug(marker, msg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg) {
        logger.debug(marker, format, arg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        logger.debug(marker, format, arg1, arg2);
    }

    @Override
    public void debug(Marker marker, String format, Object... argArray) {
        logger.debug(marker, format, argArray);
    }

    @Override
    public void debug(Marker marker, String msg, Throwable t) {
        logger.debug(marker, msg, t);
    }

    @Override
    public void error(String msg) {
        logger.error(msg);
    }

    @Override
    public void error(String format, Object arg) {
        logger.error(format, arg);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        logger.error(format, arg1, arg2);
    }

    @Override
    public void error(String format, Object... argArray) {
        logger.error(format, argArray);
    }

    @Override
    public void error(String msg, Throwable t) {
        logger.error(msg, t);
    }

    @Override
    public void error(Marker marker, String msg) {
        logger.error(marker, msg);
    }

    @Override
    public void error(Marker marker, String format, Object arg) {
        logger.error(marker, format, arg);
    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        logger.error(marker, format, arg1, arg2);
    }

    @Override
    public void error(Marker marker, String format, Object... argArray) {
        logger.error(marker, format, argArray);
    }

    @Override
    public void error(Marker marker, String msg, Throwable t) {
        logger.error(marker, msg, t);
    }

    @Override
    public void info(String msg) {
        logger.info(msg);
    }

    @Override
    public void info(String format, Object arg) {
        logger.info(format, arg);
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        logger.info(format, arg1, arg2);
    }

    @Override
    public void info(String format, Object... argArray) {
        logger.info(format, argArray);
    }

    @Override
    public void info(String msg, Throwable t) {
        logger.info(msg, t);
    }

    @Override
    public void info(Marker marker, String msg) {
        logger.info(marker, msg);
    }

    @Override
    public void info(Marker marker, String format, Object arg) {
        logger.info(marker, format, arg);
    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        logger.info(marker, format, arg1, arg2);
    }

    @Override
    public void info(Marker marker, String format, Object... argArray) {
        logger.info(marker, format, argArray);
    }

    @Override
    public void info(Marker marker, String msg, Throwable t) {
        logger.info(marker, msg, t);
    }

    @Override
    public void warn(String msg) {
        logger.warn(msg);
    }

    @Override
    public void warn(String msg, Throwable t) {
        logger.warn(msg, t);
    }

    @Override
    public void warn(String format, Object arg) {
        logger.warn(format, arg);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        logger.warn(format, arg1, arg2);
    }

    @Override
    public void warn(String format, Object... argArray) {
        logger.warn(format, argArray);
    }

    @Override
    public void warn(Marker marker, String msg) {
        logger.warn(marker, msg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg) {
        logger.warn(marker, format, arg);
    }

    @Override
    public void warn(Marker marker, String format, Object... argArray) {
        logger.warn(marker, format, argArray);
    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        logger.warn(marker, format, arg1, arg2);
    }

    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        logger.warn(marker, msg, t);
    }

    @Override
    public String toString() {
        return logger.toString();
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return logger.isDebugEnabled(marker);
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return logger.isInfoEnabled(marker);
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return logger.isTraceEnabled(marker);
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return logger.isErrorEnabled(marker);
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return logger.isWarnEnabled(marker);
    }

}
