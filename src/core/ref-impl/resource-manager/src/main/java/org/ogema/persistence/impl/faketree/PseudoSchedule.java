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
package org.ogema.persistence.impl.faketree;

import org.ogema.core.model.Resource;

/**
 * Pseudo resource type required to create a sub-tree-element holding the
 * schedule extra information.
 *
 * @deprecated not used anymore, but must not be removed either, since 
 * existing database files containing this type could not be loaded any more,
 * otherwise 
 * @author Timo Fischer, Fraunhofer IWES
 */
@Deprecated
interface PseudoSchedule extends Resource {
}
