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
package org.ogema.model.locations;

import org.ogema.core.model.units.LengthResource;
import org.ogema.model.prototypes.Data;

/**
 * Size of a physical object. The three room dimensions depend on the context.
 * For devices, length width and height should usually refer to a front-view on
 * the device.
 */
public interface PhysicalDimensions extends Data {
	/*
	 * Length or depth of the physical object       
	 */
	LengthResource length();

	/**
	 * Width of the physical object.
	 */
	LengthResource width();

	/**
	 * Height of the physical object.
	 */
	LengthResource height();
}
