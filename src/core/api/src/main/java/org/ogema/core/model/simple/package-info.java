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
 * Definition of the simple resources that contain only a single entry (in
 * contrast to their array-counterparts and schedules). For most Java
 * primitives, a corresponding resource holding a value of this type is defined
 * with a suitable name. For example, a
 * {@link org.ogema.core.model.simple.FloatResource} holds a float (note that
 * the {@link org.ogema.core.model.units.PhysicalUnitResource}s extend the float
 * resource for cases where the value represents a physical property). Two
 * notable exceptions exist:<br>
 * - the {@link org.ogema.core.model.simple.TimeResource} holds a long value. In
 * OGEMA, times shall always be given as ms since 1970 (UTC).<br>
 * - the {@link org.ogema.core.model.simple.OpaqueResource} technically is a
 * byte array. It is constructed for containing binary data, e.g. when a logo
 * shall be stored or transmitted via REST or an application's interface.<br>
 */
package org.ogema.core.model.simple;

