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

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.filter.Filter;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 *
 * @author jlapp
 */
//public class FilterAppender extends AppenderBase<ILoggingEvent> {
public class FilterAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

	final Appender<ILoggingEvent> delegate;
	final Filter<ILoggingEvent> filter;

	public FilterAppender(Appender<ILoggingEvent> delegate, Filter<ILoggingEvent> filter) {
		this.delegate = delegate;
		this.filter = filter;
		setContext(delegate.getContext());
		addFilter(filter);
	}

	@Override
	protected void append(ILoggingEvent e) {
		if (System.getSecurityManager() == null) {
			delegate.doAppend(e);
		}
		else {
			appendPrivileged(e);
		}
	}

	protected void appendPrivileged(final ILoggingEvent e) {
		AccessController.doPrivileged(new PrivilegedAction<Void>() {

			@Override
			public Void run() {
				delegate.doAppend(e);
				return null;
			}
		});
	}

}
