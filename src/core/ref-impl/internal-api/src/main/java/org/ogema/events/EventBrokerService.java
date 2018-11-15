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
package org.ogema.events;

import org.ogema.core.application.Application;

public/* OSGi-SERVICE */
interface EventBrokerService {

	/**
	 * Add an event to the event execution queue of an application. The call to the event listener callback is
	 * encapsulated inside of the EventExecutor object. If the application does not exist anymore a
	 * NoSuchApplicationException is thrown and the calling service is assumed to release all references to the
	 * application and all of its listeners. As an alternative to this mechanism weak references can be used in the
	 * calling services to hold the listeners references.
	 * 
	 * @param target
	 *            application to which to deliver the event
	 * @param exec
	 *            encapsulation of the callback listener call
	 * @throws NoSuchApplicationException
	 */
	public void addEvent(Application target, EventExecutor exec) throws NoSuchApplicationException;
}
