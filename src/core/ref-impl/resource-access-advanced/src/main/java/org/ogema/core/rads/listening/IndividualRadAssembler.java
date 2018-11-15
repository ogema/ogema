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
package org.ogema.core.rads.listening;

import java.util.HashMap;
import java.util.Map;

import org.ogema.accesscontrol.PermissionManager;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.core.rads.creation.PatternFactory;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.ResourceStructureEvent;
import org.ogema.core.resourcemanager.ResourceStructureEvent.EventType;
import org.ogema.core.resourcemanager.ResourceStructureListener;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.slf4j.Logger;
import org.ogema.core.resourcemanager.pattern.PatternListener;

/**
 * Assembly class for RADs requested by an application. Listens only to 
 * a specified set of demanded models. We create one instance of this per listener; multiple
 * pattern instances can be added.
 * @param <T>
 * @param <P> the pattern type
 */
// FIXME synchronization required?
public class IndividualRadAssembler<T extends Resource, P extends ResourcePattern<T>> extends AssemblerBase<T,P> 
		implements ResourceStructureListener {

	// Map<path, Pattern>
	private final Map<String,P> patterns = new HashMap<String, P>();
	
	public IndividualRadAssembler(ApplicationManager appMan, Logger logger, Class<P> radClass, AccessPriority writePriority, PatternListener<P> listener, 
			PatternFactory<P> factory, Object container, PermissionManager permMan) {
        super(appMan, logger, radClass, writePriority, listener, factory, container, permMan);
    }

//    public void start() {
//        // register a resource demand on the model
//        m_primaryDemandListener.start();
//    }
//    
//    public void stop() {
//        m_primaryDemandListener.stop();
//    }

	public void addInstance(P pattern) throws RuntimeException {
		String path = pattern.model.getPath();
		if (patterns.containsKey(path))
			return;
		patterns.put(path, pattern);
		if (pattern.model.isActive()) {
			m_primaryRadListener.patternAvailable(pattern);
		}
		pattern.model.addStructureListener(this);
	}
	
	// do not trigger an unavailable callback... this means that the listener has been unregistered
	public void removeInstance(P pattern) {
		String path = pattern.model.getPath();
		if (patterns.remove(path) != null) {
			removePattern(pattern);
		}
	}
	
	@Override // applies super.stop implicitly
	public void stop() {
		for (P pattern: patterns.values()) {
			removePattern(pattern);
		}
		patterns.clear();
//		super.stop();
	}
	
	private void removePattern(P pattern) {
		String path = pattern.model.getPath();
		pattern.model.removeStructureListener(this);
		CompletionListener<?> listener = m_completionListeners.remove(path);
		if (listener != null) {
			listener.stop();
		}
		availablePatterns.remove(path);
	}
	
	public int getNrOfManagedPatterns() {
		return patterns.size();
	}
	
	// registered only for the primary demanded models
	@Override
	public void resourceStructureChanged(ResourceStructureEvent event) {
		EventType type = event.getType();
        @SuppressWarnings("unchecked")
		T res = (T) event.getSource();
		String path = res.getPath();
		switch(type) {
		case RESOURCE_ACTIVATED:
		case RESOURCE_CREATED:
			if (res.isActive() && !m_completionListeners.containsKey(path)) {
				P pattern = patterns.get(path);
				if (pattern == null)
					throw new NullPointerException(m_radClass.getSimpleName() 
							+ " pattern corresponding to " + res.getPath() + " not found");
				m_primaryRadListener.patternAvailable(pattern);
			}
			break;
		case RESOURCE_DELETED:
		case RESOURCE_DEACTIVATED:
			P pattern = patterns.get(path);
			if (pattern != null) {
				m_primaryRadListener.patternUnavailable(pattern);
			}
			break;
		default:
			return;
		}
	}

}
