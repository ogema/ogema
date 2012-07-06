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
package org.ogema.core.rads.listening;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.Resource;
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
    final Constructor<P> m_constructor;
    final Map<String, P> m_matches = new HashMap<>();
    final OgemaLogger m_log;

    public PrimaryDemandListener(ApplicationManager appMan, Class<T> resType, Class<P> radType, PatternListener<P> listener) {
        m_resAcc = appMan.getResourceAccess();
        m_resType = resType;
        m_radType = radType;
        m_radListener = listener;
        m_log = appMan.getLogger();
        try {
            m_constructor = m_radType.getConstructor(Resource.class);
        } catch (NoSuchMethodException | SecurityException ex) {
            throw new RuntimeException("Could not find default constructor on Resource Pattern of type "+radType.getName()+". Resource Pattern definitions must contain the public constructor Pattern(Resource).", ex);
        }
    }
    
    @SuppressWarnings("rawtypes")
    final ResourceDemandListener<T> m_demandListener = new ResourceDemandListener<T>() {

        @Override
        public void resourceAvailable(T resource) {
            if (!resource.getPath().equals(resource.getLocation())) { // simple sanity check
                throw new RuntimeException("Framework error: Receive resourceAvailable callback with path="+resource.getPath()+" not equal to location="+resource.getLocation()+". Please report this bug to the framework developers!");
            }
            if (m_matches.containsKey(resource.getLocation())) { // sanity check.
                // FIXME we should prevent running into this issue. Once this is done, make it a proper exception again.
                m_log.warn("Framework error: Primary demand listener for demand "+m_resType.getCanonicalName()+" of RAD-type "+m_radType.getCanonicalName()+
                         " received resourceAvailable callback for resource "+resource.getLocation()+", but map of demand matches already contains this. Ignoring the callback.");
                return;
            }
            P rad;
            try {
                rad = m_constructor.newInstance(resource);
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                throw new RuntimeException("could not create a RAD object", ex);
            }
            m_matches.put(resource.getLocation(), rad);
            m_radListener.patternAvailable(rad);
        }

        @Override
        public void resourceUnavailable(T resource) {
            Objects.requireNonNull(resource);
            final P rad = m_matches.get(resource.getLocation());
            if (rad==null) {                               
                throw new RuntimeException("Framework error: Reveiced resourceUnavailable callback for resource "+resource.getLocation()+", but no primary RAD had been created for it in the first place. Please report this error to the OGEMA developers.");
            }
            m_matches.remove(resource.getLocation());
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
