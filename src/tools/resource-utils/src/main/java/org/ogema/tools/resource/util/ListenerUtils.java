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
package org.ogema.tools.resource.util;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.core.model.ValueResource;
import org.ogema.core.resourcemanager.ResourceStructureListener;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.core.resourcemanager.pattern.PatternChangeListener;
import org.ogema.tools.listener.util.RegisteredStructureValueListener;
import org.ogema.tools.listener.util.StructureValueListener;
import org.ogema.tools.listener.util.TransitiveStructureListener;
import org.ogema.tools.listener.util.TransitiveValueListener;
import org.ogema.tools.listener.util.impl.RegisteredStructureValueListenerImpl;
import org.ogema.tools.listener.util.impl.TransitiveStructureListenerImpl;
import org.ogema.tools.listener.util.impl.TransitiveValueListenerImpl;

/**
 * Note: the ListenerUtils are still in experimental status. In particular, the transitive listeners are currently safe to use 
 * only on subtrees without references. See tests for the detailed status of supported features. <br>
 * 
 * Provides convenience methods for listener handling, such as transitive value listeners (value listeners that are applied also to subresources), and combined 
 * structure and value listeners.<br>
 * Note: A {@link PatternChangeListener} is usually much more efficient than the transitive listeners provided here.
 * Only for very short resource trees the transitive listeners may be suitable. Contrary to PatternChangeListeners, the transitive listeners
 * receive one callback per modified resource, even if the modifications are made in a transaction.
 * 
 * @see ResourceValueListener
 * @see ResourceStructureListener 
 * @see PatternChangeListener
 */
public class ListenerUtils {
	
	// no need to construct this
	private ListenerUtils() {}
	
	/**
	 * Register a combined value and structure listener, that is informed about
	 * <ul>
	 * 	<li>all value changes of the resource</li>
	 *  <li>EventType#RESOURCE_ACTIVATED structure events</li>
	 *  <li>EventType#RESOURCE_DEACTIVATED structure events</li>
	 *  <li>EventType#RESOURCE_DELETED structure events</li>
	 * </ul>
	 * It should be thought of as an enhanced value listener, since when dealing with value changes, one often has 
	 * to react to the above structure changes as well, whereas others, such as SUBRESOURCE_ADDED, can be ignored.
	 * 
	 * @param resource
	 * @param listener
	 * @return
	 */
	public static <T extends Resource> RegisteredStructureValueListener<T> registerEnhancedValueListener(ValueResource resource, StructureValueListener<T> listener) {
		return new RegisteredStructureValueListenerImpl<T>(resource, listener, false);
	}
	
	/**
	 * Register a combined value and structure listener, that is informed about all
	 * value and all structure changes of the resource. This is essentially equivalent to calling<br>
	 * <code>
	 * 	resource.addValueListener(listener);
	 *  resource.addStructureListener(listener);
	 * </code>
	 *  
	 * @param resource
	
	 * @param listener
	 * @return
	 */
	public static <T extends Resource> RegisteredStructureValueListener<T> registerStructureValueListener(ValueResource resource, StructureValueListener<T> listener) {
		return new RegisteredStructureValueListenerImpl<T>(resource, listener, true);
	}
	
	
	/**
	 * Registers the passed value listener for all subresources of <code>topNode</code> of type {@link ValueResource}, 
	 * also for those that will be added in the future. Safe to use on subtrees without references.
	 * @param topNode
	 * @param listener
	 * @return
	 */
	public static TransitiveValueListener<ValueResource> registerTransitiveValueListener(Resource topNode, ResourceValueListener<ValueResource> listener) { 
		return registerTransitiveValueListener(topNode, listener, ValueResource.class, true);
	}
	
	
	/**
	 * Registers the passed value listener for all subresources of <code>topNode</code> of type T, also for those that will be added in the future.
	 * Safe to use on subtrees without references.
	 * @param topNode
	 * @param listener
	 * @param <T>
	 * @return
	 */
	public static <T extends  Resource> TransitiveValueListener<T> registerTransitiveValueListener(Resource topNode, 
				ResourceValueListener<T> listener, Class<T> resourceType, boolean callOnEveryUpdate) { 
		return new TransitiveValueListenerImpl<T>(topNode, listener, resourceType, callOnEveryUpdate);
	}

	/**
	 * Registers the passed structure listener for all subresources of <code>topNode</code> of type T, also for those that will be added in the future.
	 * Note that using a {@link org.ogema.core.resourcemanager.pattern.ResourcePattern ResourcePattern} may often be a better option than using this method.
	 * Safe to use on subtrees without references.
	 * @param topNode
	 * @param listener
	 * @return
	 */
	public static TransitiveStructureListener registerTransitiveStructureListener(Resource topNode, ResourceStructureListener listener, ApplicationManager am) { 
		return new TransitiveStructureListenerImpl(topNode, listener, am);
	}

	
	// TODO
//	public static TransitiveStructureValueListener<ValueResource> registerStructureAndTransitiveValueListener(Resource topNode, 
//				StructureValueListener<ValueResource> listener) { 
//		
//	}
//
//	public static <T extends ValueResource> TransitiveStructureValueListener<T> registerTransitiveStructureValueListener(Resource topNode, 
//				StructureValueListener<T> listener, Class<T> resourceType, boolean callOnEveryUpdate) { 
//	}

}
