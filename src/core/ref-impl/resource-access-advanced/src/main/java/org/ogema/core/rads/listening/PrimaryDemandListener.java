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
import java.util.Objects;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.core.rads.creation.PatternFactory;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceDemandListener;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.slf4j.Logger;
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
    final Logger m_log;

    public PrimaryDemandListener(ApplicationManager appMan, Logger logger, Class<T> resType, Class<P> radType, PatternListener<P> listener, PatternFactory<P> factory) {
    	m_resAcc = appMan.getResourceAccess();
        m_resType = resType;
        m_radType = radType;
        m_radListener = listener;
        m_log = logger;
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
