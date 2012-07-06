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
package org.ogema.resourcemanager.impl.model.units;

import org.ogema.core.model.Resource;
import org.ogema.core.model.units.AngleResource;
import org.ogema.core.model.units.PhysicalUnit;
import org.ogema.resourcemanager.impl.ApplicationResourceManager;

import org.ogema.resourcemanager.virtual.VirtualTreeElement;

/**
 *
 * @author Timo Fischer, Fraunhofer IWES
 */
public class DefaultAngleResource extends UnitFloatResource implements AngleResource {

	public DefaultAngleResource(VirtualTreeElement el, Class<? extends Resource> unitResourceType, String path,
			ApplicationResourceManager resMan) {
		super(el, unitResourceType, path, resMan);
	}

	@Override
	public final PhysicalUnit getUnit() {
		return PhysicalUnit.DEGREES;
	}
}
