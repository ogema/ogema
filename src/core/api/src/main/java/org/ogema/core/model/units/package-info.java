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
/**
 * Definition of {@link org.ogema.core.model.simple.FloatResource}
 * specializations that represent a physical property. All the specializations
 * implement the interface
 * {@link org.ogema.core.model.units.PhysicalUnitResource} in addition to the
 * FloatResource. A specialization exists for every commonly-used property,
 * which also defines the unit the property is measured. The currently-supported
 * units are defined in {@link org.ogema.core.model.units.PhysicalUnit}. <br>
 *
 * OGEMA tries to provide a useful list of properties, but can never be complete
 * in this respect. If no suitable physical property is available, plain
 * {@link org.ogema.core.model.simple.FloatResource}s can be used. The unit used
 * must then be explicitly defined in the definition of the data model.
 */
package org.ogema.core.model.units;

