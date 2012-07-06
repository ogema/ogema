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
package org.ogema.core.resourcemanager.pattern;

import org.ogema.core.resourcemanager.AccessPriority;

/** 
 * Definition of a framework service that allows applications and drivers to work with
 * resource patterns.
 */
public interface ResourcePatternAccess {
	/**
	 * Adds a resource demand for a given pattern and with a certain priority for the
	 * exclusive write accessed demanded in the pattern.
	 * 
	 * @param <P> type of the resource pattern
	 * @param pattern
	 *            the pattern that shall be matched
	 * @param listener
	 *            reference to the object that is informed about pattern matches
	 * @param prio
	 *            priority of the write access demands.
	 */
	<P extends ResourcePattern<?>> void addPatternDemand(Class<P> pattern, PatternListener<P> listener,
			AccessPriority prio);

	/**
	 * Delete a registered pattern demand from the framework.
	 * 
	 * @param <P> type of the resource pattern
	 * @param pattern
	 *            class declaring the demanded resource access
	 * @param listener
	 *          listener belonging to the demand. If the listener reference passed is null, the
	 *          demand for the pattern is removed for all listeners that the application registered.
	 */
	<P extends ResourcePattern<?>> void removePatternDemand(Class<P> pattern, PatternListener<P> listener);

	/**
	 * Attempts to create a resource structure according to the given pattern. The structure is
	 * created inactive.
	 * @param <P>
	 * @param name
	 * @param radtype
	 * @return 
	 *    returns the structure created by the call. Returns null if nothing could be created.
	 */
	<P extends ResourcePattern<?>> P createResource(String name, Class<P> radtype);

	/**
	 * Activates all inactive existing fields in the given structure.
	 * @param pattern structure whose resources shall be activated.
	 */
	public void activatePattern(ResourcePattern<?> pattern);

	/**
	 * De-activates all active fields in the given structure.
	 * @param pattern structure whose resources shall be de-activated.
	 */
	public void deactivatePattern(ResourcePattern<?> pattern);

}
