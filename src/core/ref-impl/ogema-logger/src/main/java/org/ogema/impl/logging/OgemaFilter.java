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
		String effectiveLevelName = effectiveLevel == LogLevel.WARNING ? "WARN" : effectiveLevel.name();
		setLevel(effectiveLevelName);
	}

	public void setLevelUser(LogLevel l) {
		userLevel = l;
		if (adminLevel == null) {
			effectiveLevel = l;
		}
        String effectiveLevelName = effectiveLevel == LogLevel.WARNING ? "WARN" : effectiveLevel.name();
		setLevel(effectiveLevelName);
	}

	public void unsetAdminLevel() {
		adminLevel = null;
		effectiveLevel = userLevel;
		String effectiveLevelName = effectiveLevel == LogLevel.WARNING ? "WARN" : effectiveLevel.name();
		setLevel(effectiveLevelName);
	}

	@Override
	public FilterReply decide(ILoggingEvent event) {
		if (effectiveLevel.equals(LogLevel.NO_LOGGING)) {
			return FilterReply.DENY;
		}
		return super.decide(event);
	}

}
