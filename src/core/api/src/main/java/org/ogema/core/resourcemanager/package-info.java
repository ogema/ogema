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
 * Defines the "basic access" to resources. The main acccess classes are the
 * {@link ResourceAccess}, defining the access methods and the {@link ResourceManagement}
 * providing methods for creation of top-level resources and deletion of resources
 * by-name. Note that a lot of resource-manipulation methods are defined on the
 * {@link org.ogema.core.model.Resource} itself, so the ResourceManagement is usually only required
 * to create a top-level resource.<br>
 * Access to the resources is possible in two ways:<br>
 * - Direct access: It is possible to directly search the existing resources at a
 * given time. <br>
 * - Listener-based access: Instead of searching the resource graph at a given time,
 * an application can add a {@link ResourceDemandListener}, which is as long as it is 
 * registered to the framework will be informed about all existing and newly-available
 * suitable resources, as well as in the case that such a resource becomes unavailable
 * for some reasons.<br>
 * <br>
 * In addition to the resource listener, further listeners are defined that can
 * be used to trace the state of the resource graph. Contrary to the "search listener"
 * ResourceDemandListener they are applied to explicit resources (both virtual and
 * non-virtual):<br>
 * - the {@link ResourceListener} listens to changes in the entries of {@link org.ogema.core.model.schedule.Schedule}s
 * and {@link org.ogema.core.model.SimpleResource}s.<br>
 * - the {@link ResourceStructureListener} listens to changes of the resource state
 * except the value contained. The possible events are defined in {@link ResourceStructureEvent}.<br>
 * - the {@link AccessModeListener} listens to changes in the application-specific 
 * {@link AccessMode} (events reported to the ResourceStructureListener are the same
 * for all applications).<br>
 * <br>
 * A more "advanced" form of resource access is the {@link Transaction}. Applications
 * can get a transaction object an fill it with commands to the resource graph that
 * are then performed in an "atomic" transaction, meaning that no other applications
 * see an intermediate state between the individual commands. This can be done to
 * avoid "illegal" intermediate states, e.g. if a set of individual values only make
 * sense combined.
 */
package org.ogema.core.resourcemanager;

