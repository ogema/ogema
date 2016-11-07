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
	
	public IndividualRadAssembler(ApplicationManager appMan, Class<P> radClass, AccessPriority writePriority, PatternListener<P> listener, 
			PatternFactory<P> factory, Object container, PermissionManager permMan) {
        super(appMan, radClass, writePriority, listener, factory, container, permMan);
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
