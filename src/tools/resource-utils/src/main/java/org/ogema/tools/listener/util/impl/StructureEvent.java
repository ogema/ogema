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
package org.ogema.tools.listener.util.impl;

import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.ResourceStructureEvent;

public class StructureEvent implements ResourceStructureEvent {

	private final EventType type;
	private final Resource source;
	private final Resource changed;
	
	public StructureEvent(EventType type, Resource source, Resource changed) {
		this.type = type;
		this.source = source;
		this.changed = changed;
	}
	
	@Override
	public EventType getType() {
		return type;
	}

	@Override
	public Resource getSource() {
		return source;
	}

	@Override
	public Resource getChangedResource() {
		return changed;
	}

}
