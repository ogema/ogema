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

import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.FilterReply;

import org.ogema.core.logging.LogLevel;
import org.ogema.core.logging.OgemaLogger;

/**
 * Logback ThresholdFilter with additional functions to override the log level with administrator settings as required
 * by the {@link OgemaLogger}.
 * 
 * @author jlapp
 */
public class OgemaFilter extends ThresholdFilter {
	protected LogLevel userLevel = LogLevel.INFO;
	protected LogLevel adminLevel;
	protected LogLevel effectiveLevel = userLevel;

	public OgemaFilter() {
		setLevel(effectiveLevel.name());
		start();
	}

	public void setLevelAdmin(LogLevel l) {
		adminLevel = l;
		effectiveLevel = l;
		setLevel(effectiveLevel.name());
	}

	public void setLevelUser(LogLevel l) {
		userLevel = l;
		if (adminLevel == null) {
			effectiveLevel = l;
		}
		setLevel(effectiveLevel.name());
	}

	public void unsetAdminLevel() {
		adminLevel = null;
		effectiveLevel = userLevel;
		setLevel(effectiveLevel.name());
	}

	@Override
	public FilterReply decide(ILoggingEvent event) {
		if (effectiveLevel.equals(LogLevel.NO_LOGGING)) {
			return FilterReply.DENY;
		}
		return super.decide(event);
	}

}
