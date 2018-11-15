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
package org.ogema.core.resourcemanager.pattern;

import java.util.List;

import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.CompoundResourceEvent;
import org.ogema.core.resourcemanager.transaction.ResourceTransaction;

/** 
 * Definition of a framework service that allows applications and drivers to work with
 * resource patterns.
 */
public interface ResourcePatternAccess {

	/**
	 * Attempts to create a resource structure according to the given pattern. The structure is
	 * created inactive and added as decorator to the given parent resource. If a subresource
	 * with the given name already exists and its type is compatible with the demanded model type
	 * of the pattern, then a pattern based on the existing subresource is returned. In this case,
	 * the returned pattern may be fulfilled already. If the subresource
	 * exists (or is declared as an optional element in the parent's type) and its type does not match
	 * the pattern demanded model class, then a ResourceAlreadyExistsException is thrown.
	 *  
	 * @param parent Parent resource where the decorator should be added
	 * @param <P> type of the resource pattern
	 * @param name Name of the to-be-created decorator resource of the pattern.
	 * @param patterntype Pattern class declaring the fields to be created.
	 * @return 
	 *    returns the structure created by the call. Returns null if nothing could be created.
	 */
	<P extends ResourcePattern<?>> P addDecorator(Resource parent, String name, Class<P> patterntype);

	/**
	 * @see #addDecorator(Resource, String, Class)
	 * @param parent
	 * @param name
	 * @param patterntype
	 * @param context
	 * 		Context information that may be used to initialize the pattern, via the 
	 * 		{@link ContextSensitivePattern#init()} mehtod.
	 * @return
	 */
	<P extends ContextSensitivePattern<?, C>, C> P addDecorator(Resource parent, String name, Class<P> patterntype,
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
	<P extends ResourcePattern<?>> void addPatternDemand(Class<P> pattern, PatternListener<? super P> listener,
			AccessPriority prio);

	/**
	 * @see #addPatternDemand(Class, PatternListener, AccessPriority)
	 * @param pattern
	 * @param listener
	 * @param prio
	 * @param context
	 * 		Context information that may influence whether or not a pattern is accepted, if it is evaluated in the
	 * 		pattern's {@link ContextSensitivePattern#accept()} method.<br>
	 * 		Note: it is not possible to register demands for the same pattern class with different contexts. The first
	 * 		demand will be replaced, otherwise.
	 */
	<P extends ContextSensitivePattern<?, C>, C> void addPatternDemand(Class<P> pattern, PatternListener<? super P> listener,
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
	<P extends ResourcePattern<?>> void removePatternDemand(Class<P> pattern, PatternListener<? super P> listener);

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
	 * Creates all optional resource fields of a pattern instance.
	 * @param patternType
	 * @param instance
	 * @param createAll
	 * 		if true, all non-existent resource fields are created, if false, only the optional ones. 
	 * 	 	This is only relevant if the pattern instance passed is not a match (not all conditions are satisfied).
	 * 		If, in particular, the pattern has been created using one of the <code>create</code> or <code>addDecorator</code> methods 
	 * 		of the ResourcePatternAccess, then the parameter is irrelevant, since all required fields exist already.
	 */
	<P extends ResourcePattern<?>> void createOptionalResourceFields(P instance, Class<P> patternType, boolean createAll);
	
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
	 * Get all subresource patterns of a specified resource matching a particular pattern class.
	 * If the patterns are of type {@link ContextSensitivePattern}, they may provide a custom initialization 
	 * method. Initialized patterns can be obtained through
	 * {@link #getPatterns(Class, AccessPriority, Object)}.
	 * @param type 
	 * 		Pattern type
	 * @param resource
	 * 		Parent resource
	 * @param recursive
	 * 		false: only direct subresources are allowed as models of the returned {@link ResourcePattern}s<br>
	 * 		true: all matching patterns in the resource tree below the specified parent resource are allowed TODO clarify whether this follows references
	 * @param writePriority
	 * 		see {@link AccessPriority}
	 * @return 
	 * 		a list of patterns whose {@link ResourcePattern#model} resource is a subresource of the specified 
	 * 		parent resource
  	 */
	<P extends ResourcePattern<?>> List<P> getSubresources(Resource resource, Class<P> type, boolean recursive, AccessPriority writePriority);

	/**
	 * @see #getSubresources(Resource, Class, boolean, AccessPriority)
	 * @param type 
	 * 		Pattern type
	 * @param resource
	 * 		Parent resource
	 * @param recursive
	 * 		false: only direct subresources are allowed as models of the returned {@link ResourcePattern}s<br>
	 * 		true: all matching patterns in the resource tree below the specified parent resource are allowed TODO clarify whether this follows references
	 * @param writePriority
	 * 		see {@link AccessPriority}
	 * @param context
	 * 		Context information that will be used to initialize the patterns prior to returning the list, by calling
	 * 		{@link ContextSensitivePattern#init()}. The context may also effect whether a pattern is accepted or not,
	 * 		if it is evaluated in the {@link ResourcePattern#accept()} method.
	 * @return
	 */
	<P extends ContextSensitivePattern<?, C>, C> List<P> getSubresources(Resource resource, Class<P> type, boolean recursive,
			AccessPriority writePriority, C context);
	
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
	
	/**
	 * Allows the tracking of individual pattern instances. The registered listener will only receive callbacks for
	 * the provided pattern instance. <br> 
	 * Note: this does not interfere with regular pattern demands, registered via 
	 * {@link #addPatternDemand(Class, PatternListener, AccessPriority)}. If the listener has also been registered
	 * via this method, it will receive two callbacks for the pattern instance.<br>
	 * The same listener may be registered for multiple pattern instances, in which case it will receive callbacks
	 * for all of these.<br>
	 * Use {@link #addPatternDemand(Class, PatternListener, AccessPriority)} instead, in order to receive callbacks
	 * for all pattern matches of a given type. 
	 * 
	 * @param <P> 
	 * 			  type of the resource pattern
	 * @param pattern
	 * 			  type of the resource pattern
	 * @param instance
	 *            the pattern instance for which patternAvailable and patternUnavailable callbacks will be issued
	 * @param listener
	 *            reference to the object that is informed about pattern matches
	 * @param prio
	 *            priority of the write access demands.
	 */
	<P extends ResourcePattern<?>> void addIndividualPatternDemand(Class<P> pattern, P instance, 
			PatternListener<? super P> listener, AccessPriority prio);

	/**
	 * Remove a listener that had been registered for an individual pattern demand, on the instance provided.
	 * 
	 * @see #addIndividualPatternDemand(Class, ResourcePattern, PatternListener, AccessPriority)
	 * @param <P> 
	 * 			  type of the resource pattern
	 * @param pattern
	 * 			  type of the resource pattern
	 * @param instance
	 *            the pattern instance for which patternAvailable and patternUnavailable callbacks will be issued
	 * @param listener
	 *            reference to the object that is informed about pattern matches
	 */
	<P extends ResourcePattern<?>> void removeIndividualPatternDemand(Class<P> pattern, P instance, PatternListener<? super P> listener);
	
	/**
	 * Removes all individual pattern demands, registered via
	 * {@link #addIndividualPatternDemand(Class, ResourcePattern, PatternListener, AccessPriority)}.
	 * This does not affect the listeners registered via
	 * {@link #addPatternDemand(Class, PatternListener, AccessPriority)}. 
	 * @param <P> 
	 * 			  type of the resource pattern
	 * @param pattern
	 * 			  type of the resource pattern
	 * @param listener
	 *            reference to the object that is informed about pattern matches
	 */
	<P extends ResourcePattern<?>> void removeAllIndividualPatternDemands(Class<P> pattern, PatternListener<? super P> listener);
	
	/**
	 * Are the pattern conditions satisfied? <br> 
	 * This includes checks whether all fields of Resource-type are active (and hence, exist), except
	 * for those that carry an @see Existence#required() = @see CreateMode#OPTIONAL annotation;
	 * whether the requested access modes for these fields are granted; and whether the {@link ResourcePattern#accept()}
	 * condition is satisfied.  
	 * 
 	 * @param <P> 
	 * 		type of the resource pattern
	 * @param instance
	 * 		the pattern instance to be checked
	 * @return
	 */
	<P extends ResourcePattern<?>> boolean isSatisfied(P instance, Class<P> type);
	
	/**
	 * @see ResourcePatternAccess#isSatisfied(ResourcePattern, Class)
	 * @param instance
	 * @param type
	 * @param context
	 * 		the pattern context; this can be used for instance in the {@link ResourcePattern#accept()} method
	 * 		to implement a custom acceptance check.<br>
	 * 		Note that the framework will set the {@link ContextSensitivePattern#context} field of <code>instance</code>
	 * 		to the provided <code>context</code> argument.
	 * @return
	 */
	<P extends ContextSensitivePattern<?, C>,C> boolean isSatisfied(P instance, Class<P> type, C context);
	
	/**
	 * Register a listener that is informed about changes in the resources of the pattern instance. This applies to
	 * both structural changes (@see org.ogema.core.resourcemanager.ResourceStructureListener ResourceStructureListener)
	 * and value changes (@see org.ogema.core.resourcemanager.ResourceValueListener ResourceValueListener).
	 * Note that changes are only reported for those fields of the pattern which carry an annotation of type 
	 * {@link ResourcePattern.ChangeListener ChangeListener}. Which changes are reported can be configured via the options of
	 * ChangeListener, e.g. {@link ResourcePattern.ChangeListener#structureListener()} defines whether structure changes 
	 * lead to a callback, and {@link ResourcePattern.ChangeListener#valueListener()} is responsible for the value listener
	 * configuration. 
	 * <br>
	 * If there are changes in more than one resource field of the pattern, and these changes have been performed in a single atomic
	 * {@link ResourceTransaction}, then they are reported in a single callback. The modifications
	 * are passed to the {@link PatternChangeListener#patternChanged(ResourcePattern, List)} method in a list of 
	 * {@link CompoundResourceEvent}, unlike the callbacks of an ordinary Structure- and ValueListener).
	 * 
	 * If multiple changes occur in the pattern fields but they are not executed in a single transaction, then the service need not
	 * guarantee that the changes lead to multiple callbacks. Instead, changes that occur in rapid succession may be reported in
	 * one callback only. 
	 * <br>
	 * The pattern instance need not be a pattern match.
	 * 
	 * @param instance
	 * @param listener
	 * @param type
	 */
	<P extends ResourcePattern<?>> void addPatternChangeListener(P instance, PatternChangeListener<? super P> listener, Class<P> type);
	
	/**
	 * Remove a pattern change listener.
	 * 
	 * @param instance
	 * @param listener
	 */
	<P extends ResourcePattern<?>> void removePatternChangeListener(P instance, PatternChangeListener<? super P> listener);
}
