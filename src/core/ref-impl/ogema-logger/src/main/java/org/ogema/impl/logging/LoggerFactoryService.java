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

import java.util.List;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.administration.AdminLogger;
import org.ogema.core.logging.LoggerFactory;
import org.ogema.core.logging.OgemaLogger;

/**
 * Provides access to the DefaultLoggerFactory singleton as an OSGi service.
 * 
 * @author jlapp
 */
@Component(immediate = true)
@Service(LoggerFactory.class)
public class LoggerFactoryService implements LoggerFactory {

	@Override
	public OgemaLogger getLogger(Class<?> clazz) {
		return DefaultLoggerFactory.INSTANCE.getLogger(clazz);
	}

	@Override
	public OgemaLogger getLogger(String name) {
		return DefaultLoggerFactory.INSTANCE.getLogger(name);
	}

	public List<AdminLogger> getAllLoggers() {
		return DefaultLoggerFactory.INSTANCE.getAdminLoggers();
	}
	
	// https://logback.qos.ch/manual/appenders.html
	// Even if a SocketAppender is no longer attached to any logger, it will not be garbage collected 
	// in the presence of a connector thread. A connector thread exists only if the connection to the server is down. 
	// To avoid this garbage collection problem, you should close the SocketAppender explicitly. Long lived applications 
	// which create/destroy many SocketAppender instances should be aware of this garbage collection problem. 
	// Most other applications can safely ignore it. 
	// TODO 
//	@Deactivate
//	protected void deactivate() {
//	}
//	@Activate
//	protected void activate() {
//	}

}
