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
		if (o== null || !(o instanceof RequestedDemand))
			return false;
		final RequestedDemand demand = (RequestedDemand) o;
		return (demand.m_pattern == m_pattern && demand.m_listener.equals(m_listener));
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
