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

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.filter.Filter;

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
		delegate.doAppend(e);
	}

}
