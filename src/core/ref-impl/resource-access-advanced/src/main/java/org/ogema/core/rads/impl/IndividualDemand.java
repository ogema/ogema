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
