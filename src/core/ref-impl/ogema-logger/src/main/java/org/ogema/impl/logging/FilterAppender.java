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
package org.ogema.impl.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.filter.Filter;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 *
 * @author jlapp
 */
public class FilterAppender extends AppenderBase<ILoggingEvent> {

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
