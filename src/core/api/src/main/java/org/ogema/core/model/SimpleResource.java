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
package org.ogema.core.model;

import org.ogema.core.model.array.ArrayResource;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.model.simple.SingleValueResource;

/**
 * This is a marker interface for simple resources. All interfaces defining resources that contain actual
 * values (except for schedules) have to define this interface.
 * @deprecated this marker interface was not defined in a useful manner. Use {@link ValueResource} and its specializations {@link ArrayResource}, {@link Schedule} and {@link SingleValueResource}, instead.
 * 
 */
public interface SimpleResource extends Resource {

}
