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
package org.slf4j.impl;

import org.slf4j.spi.LoggerFactoryBinder;
import org.ogema.core.logging.LoggerFactory;
import org.ogema.impl.logging.DefaultLoggerFactory;

/**
 * Provides the OgemaLoggerFactory instance to the slf4j logging framework. <br>
 * Required by slf4j. Original description: The binding of LoggerFactory class with an actual instance of ILoggerFactory
 * is performed using information returned by this class.
 */
public class StaticLoggerBinder implements LoggerFactoryBinder {

	public static String REQUESTED_API_VERSION = "1.6"; // !final

	private static final StaticLoggerBinder SINGLETON = new StaticLoggerBinder();

	@Override
	public LoggerFactory getLoggerFactory() {
		return DefaultLoggerFactory.INSTANCE;
	}

	@Override
	public String getLoggerFactoryClassStr() {
		return DefaultLoggerFactory.class.getName();
	}

	public static StaticLoggerBinder getSingleton() {
		return SINGLETON;
	}

}
