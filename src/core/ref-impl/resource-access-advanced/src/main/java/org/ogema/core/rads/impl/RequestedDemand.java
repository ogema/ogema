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
package org.ogema.core.rads.impl;

import java.util.Objects;
import org.ogema.core.resourcemanager.pattern.PatternListener;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;

/**
 * Structure of an advanced request issued to the framework.
 */
class RequestedDemand {
	private final Class<? extends ResourcePattern<?>> m_pattern;
	private final PatternListener<? extends ResourcePattern<?>> m_listener;

	public RequestedDemand(Class<? extends ResourcePattern<?>> pattern,
			PatternListener<? extends ResourcePattern<?>> listener) {
		Objects.requireNonNull(pattern, "Requested resource access declaration may not be null.");
		Objects.requireNonNull(listener, "Listener may not be null.");
		m_pattern = pattern;
		m_listener = listener;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof RequestedDemand))
			return false;
		final RequestedDemand demand = (RequestedDemand) o;
		return (demand.m_pattern == m_pattern && demand.m_listener == m_listener);
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 79 * hash + Objects.hashCode(this.m_pattern);
		hash = 79 * hash + Objects.hashCode(this.m_listener);
		return hash;
	}

	public boolean demandsPattern(Class<? extends ResourcePattern<?>> pattern) {
		return m_pattern.equals(pattern);
	}
}
