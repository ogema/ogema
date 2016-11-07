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
import java.util.Iterator;

import org.ogema.accesscontrol.AdminPermission;
import org.ogema.accesscontrol.PermissionManager;
import org.ogema.core.rads.listening.IndividualRadAssembler;
import org.ogema.core.rads.listening.RadAssembler;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.ogema.core.administration.RegisteredPatternListener;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.Resource;
import org.ogema.core.rads.change.PatternChangeListenerRegistration;
import org.ogema.core.rads.creation.DefaultPatternFactory;
import org.ogema.core.rads.creation.PatternFactory;
import org.ogema.core.rads.creation.RadCreator;
import org.ogema.core.rads.tools.PatternFinder;
import org.ogema.core.rads.tools.RadFactory;
import org.ogema.core.rads.tools.ResourceFieldInfo;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.patternaccess.AdministrationPatternAccess;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.core.resourcemanager.pattern.PatternListener;
import org.ogema.core.resourcemanager.pattern.ContextSensitivePattern;
import org.ogema.core.resourcemanager.pattern.PatternChangeListener;

/**
 * @author Timo Fischer, Fraunhofer IWES
 * @author cnoelle
 */
public class AdvancedAccessImpl implements AdministrationPatternAccess {

    private final OgemaLogger m_logger;
    private final ApplicationManager m_appMan;
//    private final ResourceManagement m_resMan;
    private final ResourceAccess m_resAcc;
    private final PermissionManager m_permMan;
 // synchronization only necessary due to administration access; otherwise we could use HashMap
    private final Map<RequestedDemand, RadAssembler<?,?>> m_assemblers = new ConcurrentHashMap<>();
    private final Map<RequestedDemand, IndividualRadAssembler<?, ?>> m_individual_assemblers = new ConcurrentHashMap<>();
    private final Map<RequestedChangeDemand,PatternChangeListenerRegistration> changeListeners = new ConcurrentHashMap<>();
    private volatile boolean active = true;

    public AdvancedAccessImpl(ApplicationManager appManager, PermissionManager permMan) {
        m_appMan = appManager;
        m_logger = appManager.getLogger();
//        m_resMan = appManager.getResourceManagement();
        m_resAcc = appManager.getResourceAccess();
        m_permMan = permMan; 
        
    }
    
    @Override
    public void close() {
    	active = false;
    	// unregister listeners!
    	Iterator<RadAssembler<?,?>> it = m_assemblers.values().iterator();
    	while (it.hasNext()) {
    		RadAssembler<?, ?> rad = it.next();
    		try {
    			rad.stop();
    		} catch (Exception e) {}
    		it.remove();
    	}
    	Iterator<IndividualRadAssembler<?,?>> itInd = m_individual_assemblers.values().iterator();
    	while (itInd.hasNext()) {
    		IndividualRadAssembler<?, ?> rad = itInd.next();
    		try {
    			rad.stop();
    		} catch (Exception e) {}
    		itInd.remove();
    	}
    	Iterator<PatternChangeListenerRegistration> itCh = changeListeners.values().iterator();
    	while (itCh.hasNext()) {
    		PatternChangeListenerRegistration rad = itCh.next();
    		try {
    			rad.destroy();
    		} catch (Exception e) {}
    		itCh.remove();
    	}
    }
    
    @Override
    public <P extends ResourcePattern<?>> void addPatternDemand(Class<P> clazz, PatternListener<? super P> listener, AccessPriority prio) {
    	addPatternDemand(clazz, listener, prio, new DefaultPatternFactory<P>(clazz));
    }
    
	public <P extends ResourcePattern<?>> void addPatternDemand(Class<P> clazz, PatternListener<? super P> listener, AccessPriority prio, PatternFactory<P> factory) {
		if (!active)
			return;
        final RequestedDemand demand = new RequestedDemand(clazz, listener);
        if (m_assemblers.containsKey(demand)) {
            m_logger.warn("Resource demand for class " + clazz.getCanonicalName() + " and listener " + listener.toString() + " has been requested, but the same demand already been registered previously. Will ignore the request.");
            return;
        }
        @SuppressWarnings({"rawtypes", "unchecked"})
        final RadAssembler assembler = new RadAssembler(m_appMan, clazz, prio, listener, factory, null, m_permMan);
        assembler.start();
        m_assemblers.put(demand, assembler);		
	}
	
	@Override
	public <P extends ContextSensitivePattern<?, C>, C> void addPatternDemand(Class<P> clazz, PatternListener<? super P> listener, AccessPriority prio, C container) {
		final RequestedDemand demand = new RequestedDemand(clazz, listener);
		if (!active)
			return;
        if (m_assemblers.containsKey(demand)) {
            m_logger.warn("Resource demand for class " + clazz.getCanonicalName() + " and listener " + listener.toString() + " has been requested, but the same demand already been registered previously. Will ignore the request.");
            return;
        }
        @SuppressWarnings({"rawtypes", "unchecked"})
        final RadAssembler assembler = new RadAssembler(m_appMan, clazz, prio, listener, new DefaultPatternFactory<P>(clazz),container, m_permMan);
        assembler.start();
        m_assemblers.put(demand, assembler);		
	}

    private void removeAllDemandsForPattern(Class<? extends ResourcePattern<?>> pattern) {
		if (!active)
			return;
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
    public <P extends ResourcePattern<?>> void removePatternDemand(Class<P> pattern, PatternListener<? super P> listener) {
		if (!active)
			return;
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
		if (!active)
			return null;
        final RadCreator creator = new RadCreator(m_appMan, (Class<? extends ResourcePattern<?>>) radtype, factory, null);
        creator.addDecorator(parent, name);
        return (P) creator.getRad();
	}
	

	@Override
	@SuppressWarnings({"unchecked", "rawtypes"})
	public <P extends ContextSensitivePattern<?, C>, C> P addDecorator(Resource parent, String name, Class<P> radtype, C container) {
		if (!active)
			return null;
		final RadCreator creator = new RadCreator(m_appMan, (Class<? extends ResourcePattern<?>>) radtype, new DefaultPatternFactory<P>(radtype), container);
        creator.addDecorator(parent, name);
        return (P) creator.getRad();
	}


    @Override
    public <PATTERN extends ResourcePattern<?>> PATTERN createResource(String name, Class<PATTERN> radtype) {
		if (!active)
			return null;
    	return createResource(name, radtype, new DefaultPatternFactory<PATTERN>(radtype));
    }
    

    @SuppressWarnings({"unchecked", "rawtypes"})
	public <P extends ResourcePattern<?>> P createResource(String name,	Class<P> radtype, PatternFactory<P> factory) {
		if (!active)
			return null;
		final RadCreator creator = new RadCreator(m_appMan, (Class<? extends ResourcePattern<?>>) radtype, factory,null);
        creator.create(name);
        return (P) creator.getRad();
	}
	

	@Override
	@SuppressWarnings({"unchecked", "rawtypes"})
	public <P extends ContextSensitivePattern<?, C>, C> P createResource(String name, Class<P> radtype, C container) {
		if (!active)
			return null;
		final RadCreator creator = new RadCreator(m_appMan, (Class<? extends ResourcePattern<?>>) radtype, new DefaultPatternFactory<P>(radtype), container);
        creator.create(name);
        return (P) creator.getRad();
	}

	// FIXME use transaction
    @SuppressWarnings("rawtypes")
	@Override
    public void activatePattern(ResourcePattern<?> pattern) {
		if (!active)
			return;
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

    // FIXME use transaction
    @SuppressWarnings("rawtypes")
	@Override
    public void deactivatePattern(ResourcePattern<?> pattern) {
		if (!active)
			return;
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
		if (!active)
			return null;
		PatternFinder<P> finder = new PatternFinder<P>(m_resAcc, type, factory, writePriority);
		return finder.getAllPatterns(null, false);
	}
	

	@Override
	public <P extends ContextSensitivePattern<?, C>, C> List<P> getPatterns(Class<P> type, AccessPriority writePriority, C container) {
		if (!active)
			return null;
		PatternFinder<P> finder = new PatternFinder<P>(m_resAcc, type, new DefaultPatternFactory<P>(type), writePriority, container);
		return finder.getAllPatterns(null, false);
	}


	@Override
	public <P extends ResourcePattern<?>> List<P> getSubresources(Resource resource, Class<P> type, 
			boolean recursive, AccessPriority writePriority) {
		if (!active)
			return null;
		PatternFinder<P> finder = new PatternFinder<P>(m_resAcc, type,  new DefaultPatternFactory<P>(type), writePriority);
		return finder.getAllPatterns(resource, recursive);
	}

	@Override
	public <P extends ContextSensitivePattern<?, C>, C> List<P> getSubresources(Resource resource, Class<P> type, boolean recursive,
			AccessPriority writePriority, C context) {
		if (!active)
			return null;
		PatternFinder<P> finder = new PatternFinder<P>(m_resAcc, type,  new DefaultPatternFactory<P>(type), writePriority, context);
		return finder.getAllPatterns(resource, recursive);
	}

	@Override
	public List<RegisteredPatternListener> getRegisteredPatternListeners() {
		if (!active)
			return null;
		checkAdminPermission();
		return new ArrayList<RegisteredPatternListener>(m_assemblers.values());
	}

	// FIXME can the pattern type be derived from the listener? 
	@Override
	public <P extends ResourcePattern<?>> RegisteredPatternListener getListenerInformation(PatternListener<P> listener,	Class<P> pattern) {
		if (!active)
			return null;
		checkAdminPermission();
		if (listener == null || pattern == null) return null;
		return m_assemblers.get(new RequestedDemand(pattern, listener));
	}
	
	/*
	 * Copied from ApplicationResourceManager
	 */
	private void checkAdminPermission() {
		if (System.getSecurityManager() != null
				&& !m_permMan.handleSecurity(new AdminPermission(AdminPermission.APP)))
			throw new SecurityException("Operation requires application administration permission");
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public <P extends ResourcePattern<?>> void addIndividualPatternDemand(Class<P> pattern, P instance, 
			PatternListener<? super P> listener, AccessPriority prio) {
		if (!active)
			return;
		final RequestedDemand demand = new RequestedDemand(pattern, listener);
		IndividualRadAssembler assembler = m_individual_assemblers.get(demand);
		if (assembler == null) {
			assembler = new IndividualRadAssembler(m_appMan, pattern, prio, listener, 
					new DefaultPatternFactory<P>(pattern), null, m_permMan);
			m_individual_assemblers.put(demand, assembler);
		}
		assembler.addInstance(instance);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public <P extends ResourcePattern<?>> void removeIndividualPatternDemand(Class<P> pattern, P instance, PatternListener<? super P> listener) {
		if (!active)
			return;
		final RequestedDemand demand = new RequestedDemand(pattern, listener);
		IndividualRadAssembler assembler = m_individual_assemblers.get(demand);
		if (assembler != null) {
			assembler.removeInstance(instance);
			if (assembler.getNrOfManagedPatterns() == 0) {
				m_individual_assemblers.remove(demand);
			}
		}
	}
	
	@SuppressWarnings({ "rawtypes" })
	@Override
	public <P extends ResourcePattern<?>> void removeAllIndividualPatternDemands(
			Class<P> pattern, PatternListener<? super P> listener) {
		if (!active)
			return;
		final RequestedDemand demand = new RequestedDemand(pattern, listener);
		IndividualRadAssembler assembler = m_individual_assemblers.remove(demand);
		if (assembler != null) 
			assembler.stop();
	}

	@Override
	public <P extends ResourcePattern<?>> boolean isSatisfied(P instance, Class<P> type) {
		if (!active)
			return false;
		PatternFinder<P> finder = new PatternFinder<P>(m_resAcc, type,  
				new DefaultPatternFactory<P>(type), AccessPriority.PRIO_LOWEST, null);
		return finder.isSatisfied(instance);
	}
	
	@Override
	public <P extends ContextSensitivePattern<?, C>, C> boolean isSatisfied(P instance, Class<P> type, C context) {
		if (!active)
			return false;
		PatternFinder<P> finder = new PatternFinder<P>(m_resAcc, type,  
				new DefaultPatternFactory<P>(type), AccessPriority.PRIO_LOWEST, context);
		return finder.isSatisfied(instance);
	}
	
	@Override
	public <P extends ResourcePattern<?>> void createOptionalResourceFields(P instance, Class<P> patternType, boolean createAll) {
		if (!active)
			return;
		PatternFinder<P> finder = new PatternFinder<P>(m_resAcc, patternType,  
				new DefaultPatternFactory<P>(patternType), AccessPriority.PRIO_LOWEST, null);
		finder.createOptionalFields(instance, createAll);
	}

	@Override
	public <P extends ResourcePattern<?>> void addPatternChangeListener(P instance, 
				PatternChangeListener<? super P> listener, Class<P> type) {
		if (!active)
			return;
		RequestedChangeDemand demand = new RequestedChangeDemand(instance, listener);
		if (changeListeners.containsKey(demand)) {
			m_logger.warn("Pattern change listener for " + instance + " has already been registered");
			return;
		}
		PatternChangeListenerRegistration registration 
			= new PatternChangeListenerRegistration(instance, m_appMan, listener, type);
		changeListeners.put(demand, registration);
	}
	
	@Override
	public <P extends ResourcePattern<?>> void removePatternChangeListener(P instance, PatternChangeListener<? super P> listener) {
		if (!active)
			return;
		RequestedChangeDemand demand = new RequestedChangeDemand(instance, listener);
		PatternChangeListenerRegistration registration = changeListeners.remove(demand);
		if (registration != null)
			registration.destroy();
	}
}
