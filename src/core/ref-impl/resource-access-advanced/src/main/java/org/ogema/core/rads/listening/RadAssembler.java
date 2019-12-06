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

import org.ogema.accesscontrol.PermissionManager;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.core.rads.creation.PatternFactory;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.slf4j.Logger;
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

	public RadAssembler(ApplicationManager appMan, Logger logger, Class<P> radClass, AccessPriority writePriority, PatternListener<P> listener, 
			PatternFactory<P> factory, Object container, PermissionManager permMan) {
        super(appMan, logger, radClass, writePriority, listener, factory, container, permMan);
        m_primaryDemandListener = new PrimaryDemandListener<>(m_appMan, m_logger, m_demandedModelType, m_radClass, m_primaryRadListener, factory);                
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
