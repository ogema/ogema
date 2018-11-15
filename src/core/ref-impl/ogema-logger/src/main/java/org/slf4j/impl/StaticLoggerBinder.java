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
