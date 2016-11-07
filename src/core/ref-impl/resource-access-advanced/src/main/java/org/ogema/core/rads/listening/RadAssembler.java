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

import org.ogema.accesscontrol.PermissionManager;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.core.rads.creation.PatternFactory;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.core.resourcemanager.pattern.PatternListener;

/**
 * Assembly class for RADs requested by an application. Registers a primary
 * demanded model listener.<br>
 * @author Timo Fischer, Fraunhofer IWES
 * @param <T>
 * @param <P> the pattern type
 */
public class RadAssembler<T extends Resource, P extends ResourcePattern<T>> extends AssemblerBase<T,P> {

    private final PrimaryDemandListener<T, P> m_primaryDemandListener;

	public RadAssembler(ApplicationManager appMan, Class<P> radClass, AccessPriority writePriority, PatternListener<P> listener, 
			PatternFactory<P> factory, Object container, PermissionManager permMan) {
        super(appMan, radClass, writePriority, listener, factory, container, permMan);
        m_primaryDemandListener = new PrimaryDemandListener<>(m_appMan, m_demandedModelType, m_radClass, m_primaryRadListener, factory);                
    }

    public void start() {
        // register a resource demand on the model
        m_primaryDemandListener.start();
    }
    
    @Override
    public void stop() {
        m_primaryDemandListener.stop();
        super.stop();
    }
    
}
