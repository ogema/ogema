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
package org.ogema.rest.servlet;

import org.ogema.core.model.Resource;

public class ResourceRequestInfo {

	final Resource resource;

	final boolean schedule;

	final long start;

	final long end;

	public ResourceRequestInfo(Resource resource, boolean schedule, long start, long end) {
		this.resource = resource;
		this.schedule = schedule;
		this.start = start;
		this.end = end;
	}

	public boolean isSchedule() {
		return schedule;
	}

	public Resource getResource() {
		return resource;
	}

	public long getStart() {
		return start;
	}

	public long getEnd() {
		return end;
	}

}
