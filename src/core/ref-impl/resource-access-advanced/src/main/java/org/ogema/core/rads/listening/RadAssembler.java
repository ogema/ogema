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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.core.rads.tools.RadFactory;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.core.resourcemanager.pattern.PatternListener;
import org.slf4j.Logger;

/**
 * Assembly class for RADs requested by an application.<br>
 * @author Timo Fischer, Fraunhofer IWES
 * @param <T>
 * @param <P> the pattern type
 */
public class RadAssembler<T extends Resource, P extends ResourcePattern<T>> {

    private final ApplicationManager m_appMan;
    private final Logger logger;
    private final PatternListener<P> m_listener;
    
    private final PrimaryDemandListener<T, P> m_primaryDemandListener;
    private final Class<P> m_radClass;

    private final RadFactory<T,P> m_factory;
    private final Map<String,CompletionListener<P>> m_completionListeners = new HashMap<>();
    
    public RadAssembler(ApplicationManager appMan, Class<P> radClass, AccessPriority writePriority, PatternListener<P> listener) {
        m_appMan = appMan;
        logger = appMan.getLogger();
        m_listener = listener;        
        m_radClass = radClass;
        
        m_factory = new RadFactory<>(radClass, writePriority);
        m_primaryDemandListener = new PrimaryDemandListener<>(m_appMan, m_factory.getDemandedModel(), m_radClass, primaryRadListener);                
    }

    public void start() {
        // register a resource demand on the model
        m_primaryDemandListener.start();
    }
    
    public void stop() {
        m_primaryDemandListener.stop();
    }
    
    private final PatternListener<P> primaryRadListener = new PatternListener<P>() {

        @Override
        public void patternAvailable(P rad) {
            final CompletionListener<P> completeListener = new CompletionListener<>(m_appMan, rad, m_factory.getResourceFieldInfos());
            completeListener.start(m_completionListener);
            m_completionListeners.put(rad.model.getLocation(),completeListener);
        }

        @Override
        public void patternUnavailable(P object2beLost) {
        	//System.out.println("  RadAssembler: unavailable " + object2beLost.model.getLocation() + ", " + m_appMan.getAppID().getIDString());
        	
            // primary demand lost. Stop all completion listeners. -> no, stop only the one for the specific rad
//            for (CompletionListener<P> listener : m_completionListeners) {
        	CompletionListener<P> listener = m_completionListeners.get(object2beLost.model.getLocation());
        	if (listener == null) {
        		logger.warn("AdvancedAccess internal error... CompletionListener found null.");
        		return;
        	}       	
            listener.stop();
//            }
//            m_completionListeners.clear();
            m_completionListeners.remove(object2beLost.model.getLocation());
            m_listener.patternUnavailable(object2beLost);
        }
    };    
    
    private final PatternListener<P> m_completionListener = new PatternListener<P>() {

        @Override
        public void patternAvailable(P fulfilledDemand) {            
             m_listener.patternAvailable(fulfilledDemand);
        }

        @Override
        public void patternUnavailable(P object2beLost) {
            m_listener.patternUnavailable(object2beLost);
        }
    };
}
