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
package org.ogema.channelmapper.impl;

import org.ogema.core.model.Resource;

public class ResourceAndMore {

	public Resource resource;
	public Double valueOffset;
	public Double scalingFactor;

	public ResourceAndMore(Resource resource, Double valueOffset, Double scalingFactor) {
		this.resource = resource;
		this.scalingFactor = scalingFactor;
		this.valueOffset = valueOffset;
		resource.activate(true);
		resource.getParent().activate(true);
	}

}
