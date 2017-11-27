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
import java.util.Objects;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.Resource;
import org.ogema.core.rads.creation.PatternFactory;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceDemandListener;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.core.resourcemanager.pattern.PatternListener;

/**
 * Listener to the DemandedModel of a ResourcePattern.
 * @author Timo Fischer, Fraunhofer IWES
 */
class PrimaryDemandListener<T extends Resource, P extends ResourcePattern<T>> {

    final ResourceAccess m_resAcc;
    final PatternListener<P> m_radListener;
    final Class<T> m_resType;
    final Class<P> m_radType;
//    final Constructor<P> m_constructor;
    final PatternFactory<P> m_factory;
    final Map<String, P> m_matches = new HashMap<>();
    final OgemaLogger m_log;

    public PrimaryDemandListener(ApplicationManager appMan, Class<T> resType, Class<P> radType, PatternListener<P> listener, PatternFactory<P> factory) {
    	m_resAcc = appMan.getResourceAccess();
        m_resType = resType;
        m_radType = radType;
        m_radListener = listener;
        m_log = appMan.getLogger();
//        try {
//            m_constructor = m_radType.getConstructor(Resource.class);
//        } catch (NoSuchMethodException | SecurityException ex) {
//            throw new RuntimeException("Could not find default constructor on Resource Pattern of type "+radType.getName()+". Resource Pattern definitions must contain the public constructor Pattern(Resource).", ex);
//        }
        m_factory = factory;
    }
    
    final ResourceDemandListener<T> m_demandListener = new ResourceDemandListener<T>() {

        @Override
        public void resourceAvailable(T resource) {
        	m_log.debug("Primary demand satisfied:{}", resource.getLocation());
            if (!resource.getPath().equals(resource.getLocation())) { // simple sanity check
            	/* 
            	 * depending on the system setup, this case may indeed occur (a resource is created, 
            	* and then immediately set as a reference); this should be considered a bad practice, 
            	* but no reason to file a bug report; we can however dismiss the resource in this case
            	*/
//              throw new RuntimeException("Framework error: Received resourceAvailable callback with path="+resource.getPath()+" not equal to location="+resource.getLocation()+". Please report this bug to the framework developers!");
            	if (m_log.isDebugEnabled())
            		m_log.debug("Pattern primary demand listener received a callback for a referencing resource. "
            			+ "Path: {}, location:_{}",resource.getPath(),resource.getLocation());
            	return;
            }
            if (m_matches.containsKey(resource.getPath())) { // sanity check.
                // FIXME we should prevent running into this issue. Once this is done, make it a proper exception again.
                m_log.error("Framework error: Primary demand listener for demand "+m_resType.getCanonicalName()+" of RAD-type "+m_radType.getCanonicalName()+
                         " received resourceAvailable callback for resource "+resource.getLocation()+", but map of demand matches already contains this. Ignoring the callback.");
                return;
            }
            P rad;
//            try {
//                rad = m_constructor.newInstance(resource);
//            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
//                throw new RuntimeException("could not create a RAD object", ex);
//            }
            try {
            	rad = m_factory.createNewPattern(resource); 
            } catch (RuntimeException e) {
            	// happens regularly
            	m_log.debug("Could not create a pattern object; probably the resource was immediately deleted "
            			+ "after being created: {}",resource,e);
            	return;
            } catch (Exception ex) {
            	throw new RuntimeException("could not create a RAD object for " + resource, ex); // XXX ?
            }
            
            m_matches.put(resource.getPath(), rad);
            m_radListener.patternAvailable(rad);
        }

        @Override
        public void resourceUnavailable(T resource) {
        	m_log.debug("Primary demand loss: {}", resource.getLocation()); 
            Objects.requireNonNull(resource);
            final P rad = m_matches.remove(resource.getPath());
            if (rad==null) {
            	/* 
            	 * may actually occur, see explanation in resourceAvailable
            	 */
                //throw new RuntimeException("Framework error: Reveiced resourceUnavailable callback for resource "+resource.getPath()+", but no primary RAD had been created for it in the first place. Please report this error to the OGEMA developers.");
            	m_log.debug("Primary demand loss, but no corresponding resource found: {}",resource);
            	return;
            }
            m_radListener.patternUnavailable(rad);
        }
    };

    void start() {
        m_resAcc.addResourceDemand(m_resType, m_demandListener);       
    }

    void stop() {
        m_resAcc.removeResourceDemand(m_resType, m_demandListener);
    }
}
