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
 * Definitions of the pattern access to resources. An application can define a
 * pattern in the resource graph by defining a class that inherits from
 * {@link org.ogema.core.resourcemanager.pattern.ResourcePattern}. The generic
 * parameter of the pattern defines the resource type of the root node. Resource
 * fields in the defining class can the be used to define the pattern. The
 * annotations in {@link org.ogema.core.resourcemanager.pattern.ResourcePattern}
 * can be added to these fields to define the required
 * {@link org.ogema.core.resourcemanager.AccessMode} (for searching commands,
 * only) as well as optional fields in the pattern. The
 * {@link org.ogema.core.resourcemanager.pattern.ResourcePatternAccess} allows
 * to create such patterns and to register
 * {@link org.ogema.core.resourcemanager.pattern.PatternListener}s that keep the
 * application updated on all matches for the pattern that exist in the resource
 * graph.
 */
package org.ogema.core.resourcemanager.pattern;

