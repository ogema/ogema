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

import java.util.HashSet;
import java.util.Set;

import org.ogema.core.resourcemanager.pattern.PatternListener;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;

public class IndividualDemand extends RequestedDemand {

	private final Set<ResourcePattern<?>> patterns = new HashSet<ResourcePattern<?>>(); // TODO synchronize?
	
	public IndividualDemand(Class<? extends ResourcePattern<?>> pattern, 
			 PatternListener<? extends ResourcePattern<?>> listener) {
		super(pattern, listener);
	}
	
	public void addPattern(ResourcePattern<?> pattern) {
		patterns.add(pattern);
	}
	
	public boolean removePattern(ResourcePattern<?> pattern) {
		return patterns.remove(pattern);
	}

	public void clear() {
		patterns.clear();
	}
	
	public Set<ResourcePattern<?>> getPatterns() {
		return new HashSet<ResourcePattern<?>>(patterns);
	}
	
}
