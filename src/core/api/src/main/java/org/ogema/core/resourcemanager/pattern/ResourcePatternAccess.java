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
package org.ogema.core.resourcemanager.pattern;

import java.util.List;

import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.AccessPriority;

/** 
 * Definition of a framework service that allows applications and drivers to work with
 * resource patterns.
 */
public interface ResourcePatternAccess {

	/**
	 * Attempts to create a resource structure according to the given pattern. The structure is
	 * created inactive and added as decorator to the given parent resource. Uses a 
	 * default pattern factory, which implies that the pattern class must have a public 
	 * standard constructor with no arguments.
	 * 
	 * @param parent Parent resource where the decorator should be added
	 * @param <P> type of the resource pattern
	 * @param name Name of the to-be-created decorator resource of the pattern.
	 * @param radtype Pattern class declaring the fields to be created.
	 * @return 
	 *    returns the structure created by the call. Returns null if nothing could be created.
	 */
	<P extends ResourcePattern<?>> P addDecorator(Resource parent, String name, Class<P> radtype);

	/**
	 * @see #addDecorator(Resource, String, Class)
	 * @param parent
	 * @param name
	 * @param radtype
	 * @param context
	 * 		Context information that may be used to initialize the pattern, via the 
	 * 		{@link ContextSensitivePattern#init()} mehtod.
	 * @return
	 */
	<P extends ContextSensitivePattern<?, C>, C> P addDecorator(Resource parent, String name, Class<P> radtype,
			C context);

	/**
	 * Adds a resource demand for a given pattern and with a certain priority for the
	 * exclusive write accessed demanded in the pattern. A default pattern factory is used 
	 * for the creation of new patterns, which implies that the pattern class must have a public 
	 * standard constructor with no arguments.
	 * 
	 * @param <P> 
	 * 			  type of the resource pattern
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
	 * @see #addPatternDemand(Class, PatternListener, AccessPriority)
	 * @param pattern
	 * @param listener
	 * @param prio
	 * @param context
	 * 		Context information that may influence whether or not a pattern is accepted, if it is evaluated in the
	 * 		pattern's {@link ContextSensitivePattern#accept()} method.
	 */
	<P extends ContextSensitivePattern<?, C>, C> void addPatternDemand(Class<P> pattern, PatternListener<P> listener,
			AccessPriority prio, C context);

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
	 * TODO
	 * 
	 * @param <P> type of the resource pattern
	 * @param name Name of the to-be-created root resource of the pattern.
	 * @param radtype Pattern class declaring the fields to be created.
	 * @return 
	 *    returns the structure created by the call. Returns null if nothing could be created.
	 */
	<P extends ResourcePattern<?>> P createResource(String name, Class<P> radtype);

	/**
	 * @see #createResource(String, Class)
	 * @param name
	 * @param radtype
	 * @param context
	 * 		Context information which may be used to initialize the new pattern, via the
	 * 		{@link ContextSensitivePattern#init()} method.
	 * @return
	 */
	<P extends ContextSensitivePattern<?, C>, C> P createResource(String name, Class<P> radtype, C context);

	/**
	 * Get all resource patterns matching a particular pattern class.
	 * If the patterns are of type {@link ContextSensitivePattern}, they may provide a custom initialization 
	 * method. Initialized patterns can be obtained through
	 * {@link #getPatterns(Class, AccessPriority, Object)}.
	 * @param type
	 * @return
	 */
	<P extends ResourcePattern<?>> List<P> getPatterns(Class<P> type, AccessPriority writePriority);

	/**
	 * @see #getPatterns(Class, AccessPriority)
	 * @param type
	 * @param writePriority
	 * @param context
	 * 		Context information that will be used to initialize the patterns prior to returning the list, by calling
	 * 		{@link ContextSensitivePattern#init()}. The context may also effect whether a pattern is accepted or not,
	 * 		if it is evaluated in the {@link ResourcePattern#accept()} method.
	 * @return
	 */
	<P extends ContextSensitivePattern<?, C>, C> List<P> getPatterns(Class<P> type, AccessPriority writePriority,
			C context);

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
