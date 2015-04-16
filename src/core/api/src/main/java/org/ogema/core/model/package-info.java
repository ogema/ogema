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
/**
 * Definition a key element of the OGEMA framework, the {@link Resource} interface.
 * Also defines the special resource type {@link ResourceList}, which tries to
 * mimic the resource equivalent of a Java List (with reduced functionality) and
 * the marker interface {@link SimpleResource} which identifies resources that
 * can contain a value or an array of values (note that schedules are not simple
 * resources). The {@link ModelModifiers} are framework-defined annotations that
 * are used in the modeling of new resource types. <br>
 * The sub-packages define the core resource types defined in OGEMA. The non-simple
 * models describing devices, relations, communication data, ... are not defined
 * in the OGEMA API but are contained in the separate package with the OGEMA Data Model.<br>
 */
package org.ogema.core.model;

