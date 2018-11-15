/**
 * Copyright 2011-2018 Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

