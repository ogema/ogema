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
