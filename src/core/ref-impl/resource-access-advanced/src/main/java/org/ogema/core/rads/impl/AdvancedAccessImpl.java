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
package org.ogema.core.rads.impl;

import java.util.ArrayList;

import org.ogema.core.rads.listening.RadAssembler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.Resource;
import org.ogema.core.rads.creation.DefaultPatternFactory;
import org.ogema.core.rads.creation.PatternFactory;
import org.ogema.core.rads.creation.RadCreator;
import org.ogema.core.rads.tools.PatternFinder;
import org.ogema.core.rads.tools.RadFactory;
import org.ogema.core.rads.tools.ResourceFieldInfo;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.core.resourcemanager.pattern.ResourcePatternAccess;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.core.resourcemanager.pattern.PatternListener;
import org.ogema.core.resourcemanager.pattern.ContextSensitivePattern;

/**
 * @author Timo Fischer, Fraunhofer IWES
 */
public class AdvancedAccessImpl implements ResourcePatternAccess {

    private final OgemaLogger m_logger;
    private final ApplicationManager m_appMan;
    private final ResourceManagement m_resMan;
    private final ResourceAccess m_resAcc;
    private final Map<RequestedDemand, RadAssembler<?,?>> m_assemblers = new HashMap<>();

    public AdvancedAccessImpl(ApplicationManager appManager) {
        m_appMan = appManager;
        m_logger = appManager.getLogger();
        m_resMan = appManager.getResourceManagement();
        m_resAcc = appManager.getResourceAccess();
    }

    @Override
    public <P extends ResourcePattern<?>> void addPatternDemand(Class<P> clazz, PatternListener<P> listener, AccessPriority prio) {
    	addPatternDemand(clazz, listener, prio, new DefaultPatternFactory<P>(clazz));
    }
    
	public <P extends ResourcePattern<?>> void addPatternDemand(Class<P> clazz, PatternListener<P> listener, AccessPriority prio, PatternFactory<P> factory) {
        final RequestedDemand demand = new RequestedDemand(clazz, listener);
        if (m_assemblers.containsKey(demand)) {
            m_logger.warn("Resource demand for class " + clazz.getCanonicalName() + " and listener " + listener.toString() + " has been requested, but the same demand already been registered previously. Will ignore the request.");
            return;
        }
        @SuppressWarnings({"rawtypes", "unchecked"})
        final RadAssembler assembler = new RadAssembler(m_appMan, clazz, prio, listener, factory, null);
        assembler.start();
        m_assemblers.put(demand, assembler);		
	}
	
	@Override
	public <P extends ContextSensitivePattern<?, C>, C> void addPatternDemand(Class<P> clazz, PatternListener<P> listener, AccessPriority prio, C container) {
		final RequestedDemand demand = new RequestedDemand(clazz, listener);
        if (m_assemblers.containsKey(demand)) {
            m_logger.warn("Resource demand for class " + clazz.getCanonicalName() + " and listener " + listener.toString() + " has been requested, but the same demand already been registered previously. Will ignore the request.");
            return;
        }
        @SuppressWarnings({"rawtypes", "unchecked"})
        final RadAssembler assembler = new RadAssembler(m_appMan, clazz, prio, listener, new DefaultPatternFactory<P>(clazz),container);
        assembler.start();
        m_assemblers.put(demand, assembler);		
	}

    private void removeAllDemandsForPattern(Class<? extends ResourcePattern<?>> pattern) {
        // find all fitting demands
        List<RequestedDemand> fittingDemands = new ArrayList<>();
        for (RequestedDemand demand : m_assemblers.keySet()) {
            if (demand.demandsPattern(pattern)) fittingDemands.add(demand);
        }
        // remove the demand and stop the corresponding assembler.
        for (RequestedDemand demand : fittingDemands) {
            final RadAssembler<?,?> assembler = m_assemblers.remove(demand);
            assert assembler != null;
            assembler.stop();
        }
    }

    @Override
    public <P extends ResourcePattern<?>> void removePatternDemand(Class<P> pattern, PatternListener<P> listener) {
        Objects.requireNonNull(pattern, "Resource pattern may not be null.");

        if (listener == null) {
            removeAllDemandsForPattern(pattern);
            return;
        }

        final RequestedDemand demand = new RequestedDemand(pattern, listener);
        if (!m_assemblers.containsKey(demand)) {
            m_logger.warn("Resource demand for class " + pattern.getCanonicalName() + " and listener " + listener.toString() + " has should be removed, but but no such demand had been registered before. Will ignore the request.");
            return;
        }

        final RadAssembler<?,?> assembler = m_assemblers.remove(demand);
        assert assembler != null;
        assembler.stop();
    }
    
    @Override
    public <PATTERN extends ResourcePattern<?>> PATTERN addDecorator(Resource parent, String name, Class<PATTERN> radtype) {
        return addDecorator(parent, name, radtype, new DefaultPatternFactory<PATTERN>(radtype));
    }
    

    @SuppressWarnings({"unchecked", "rawtypes"})
	public <P extends ResourcePattern<?>> P addDecorator(Resource parent, String name, Class<P> radtype, PatternFactory<P> factory) {
        final RadCreator creator = new RadCreator(m_appMan, (Class<? extends ResourcePattern<?>>) radtype, factory, null);
        creator.addDecorator(parent, name);
        return (P) creator.getRad();
	}
	

	@Override
	@SuppressWarnings({"unchecked", "rawtypes"})
	public <P extends ContextSensitivePattern<?, C>, C> P addDecorator(Resource parent, String name, Class<P> radtype, C container) {
		final RadCreator creator = new RadCreator(m_appMan, (Class<? extends ResourcePattern<?>>) radtype, new DefaultPatternFactory<P>(radtype), container);
        creator.addDecorator(parent, name);
        return (P) creator.getRad();
	}


    @Override
    public <PATTERN extends ResourcePattern<?>> PATTERN createResource(String name, Class<PATTERN> radtype) {
    	return createResource(name, radtype, new DefaultPatternFactory<PATTERN>(radtype));
    }
    

    @SuppressWarnings({"unchecked", "rawtypes"})
	public <P extends ResourcePattern<?>> P createResource(String name,	Class<P> radtype, PatternFactory<P> factory) {
		final RadCreator creator = new RadCreator(m_appMan, (Class<? extends ResourcePattern<?>>) radtype, factory,null);
        creator.create(name);
        return (P) creator.getRad();
	}
	

	@Override
	@SuppressWarnings({"unchecked", "rawtypes"})
	public <P extends ContextSensitivePattern<?, C>, C> P createResource(String name, Class<P> radtype, C container) {
		final RadCreator creator = new RadCreator(m_appMan, (Class<? extends ResourcePattern<?>>) radtype, new DefaultPatternFactory<P>(radtype), container);
        creator.create(name);
        return (P) creator.getRad();
	}

    @SuppressWarnings("rawtypes")
	@Override
    public void activatePattern(ResourcePattern<?> pattern) {
        Objects.requireNonNull(pattern, "Access declaration to activate must not be null.");
        @SuppressWarnings("unchecked")
        Class<ResourcePattern<?>> patternClass = (Class<ResourcePattern<?>>) pattern.getClass();
        List<ResourceFieldInfo> fields = RadFactory.getResourceInfoRecursively(patternClass, AccessPriority.PRIO_LOWEST); // access priority is irrelevant, here.
        List<Resource> resources = RadFactory.getInitializedResourcesFromParameters(fields, (ResourcePattern) pattern);
        for (Resource resource : resources) {
            if (resource.isActive()) continue;
            if (resource.exists()) resource.activate(false);
        }        
        pattern.model.activate(false);
        
    }

    @SuppressWarnings("rawtypes")
	@Override
    public void deactivatePattern(ResourcePattern<?> pattern) {
        Objects.requireNonNull(pattern, "Access declaration to de-activate must not be null.");
        @SuppressWarnings("unchecked")
        Class<ResourcePattern<?>> patternClass = (Class<ResourcePattern<?>>) pattern.getClass();
        List<ResourceFieldInfo> fields = RadFactory.getResourceInfoRecursively(patternClass, AccessPriority.PRIO_LOWEST); // access priority is irrelevant, here.
        List<Resource> resources = RadFactory.getInitializedResourcesFromParameters(fields, (ResourcePattern) pattern);
        for (Resource resource : resources) {
            if (!resource.exists()) continue;
            if (resource.isActive()) resource.deactivate(false);
        }
        pattern.model.deactivate(false);
    }

	@Override
	public <P extends ResourcePattern<?>> List<P> getPatterns(Class<P> type, AccessPriority writePriority) {
		return getPatterns(type, writePriority, new DefaultPatternFactory<P>(type));
	}

	public <P extends ResourcePattern<?>> List<P> getPatterns(Class<P> type, AccessPriority writePriority, PatternFactory<P> factory) {
		PatternFinder<P> finder = new PatternFinder<P>(m_resAcc, type, factory, writePriority);
		return finder.getAllPatterns();
	}
	

	@Override
	public <P extends ContextSensitivePattern<?, C>, C> List<P> getPatterns(Class<P> type, AccessPriority writePriority, C container) {
		PatternFinder<P> finder = new PatternFinder<P>(m_resAcc, type, new DefaultPatternFactory<P>(type), writePriority, container);
		return finder.getAllPatterns();
	}


}
