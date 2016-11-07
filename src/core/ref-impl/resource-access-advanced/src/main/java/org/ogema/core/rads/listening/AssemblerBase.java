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
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.ogema.accesscontrol.AdminPermission;
import org.ogema.accesscontrol.PermissionManager;
import org.ogema.core.administration.AdminApplication;
import org.ogema.core.administration.AdministrationManager;
import org.ogema.core.administration.PatternCondition;
import org.ogema.core.administration.RegisteredPatternListener;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.core.rads.creation.PatternFactory;
import org.ogema.core.rads.tools.RadFactory;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.pattern.PatternListener;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.slf4j.Logger;

public class AssemblerBase<T extends Resource, P extends ResourcePattern<T>> implements RegisteredPatternListener {

	protected final ApplicationManager m_appMan;
	protected final PermissionManager m_permMan;
	protected final Logger logger;
	protected final PatternListener<P> m_listener;

	protected final Class<P> m_radClass;
	protected final Class<T> m_demandedModelType;

	protected final RadFactory<T, P> m_factory;

	// synchronization only necessary due to administration access; otherwise we
	// could use HashMap and LinkedList
	protected final Map<String, CompletionListener<P>> m_completionListeners = new ConcurrentHashMap<>();
	protected final List<String> availablePatterns = Collections.synchronizedList(new LinkedList<String>());

	protected final Object m_container;

	public AssemblerBase(ApplicationManager appMan, Class<P> radClass, AccessPriority writePriority, PatternListener<P> listener,
			PatternFactory<P> factory, Object container, PermissionManager permMan) {
		m_appMan = appMan;
		logger = appMan.getLogger();
		m_listener = listener;
		m_radClass = radClass;
		m_container = container;
		m_permMan = permMan;
		m_factory = new RadFactory<>(radClass, writePriority, factory);
		m_demandedModelType = m_factory.getDemandedModel();
	}
	
	protected void stop() {
		Iterator<CompletionListener<P>> it = m_completionListeners.values().iterator();
		while (it.hasNext()) {
			CompletionListener<P> cl = it.next();
			try {
				cl.stop();
			} catch (Exception e) {}
			it.remove();
		}
		synchronized (availablePatterns) {
			availablePatterns.clear();
		}
	}

	
	protected final PatternListener<P> m_primaryRadListener = new PatternListener<P>() {

		@Override
		public void patternAvailable(P rad) {
			final CompletionListener<P> completeListener = new CompletionListener<>(m_appMan, rad,
					m_factory.getResourceFieldInfos(), m_container);
			completeListener.start(m_completionListener);
			m_completionListeners.put(rad.model.getPath(), completeListener);
		}

		@Override
		public void patternUnavailable(P object2beLost) {
			// System.out.println("  RadAssembler: unavailable " +
			// object2beLost.model.getLocation() + ", " +
			// m_appMan.getAppID().getIDString());

			// primary demand lost. Stop all completion listeners for the
			// specific rad
			// for (CompletionListener<P> listener : m_completionListeners) {
			CompletionListener<P> listener = m_completionListeners.remove(object2beLost.model.getPath());
			if (listener == null) {
				logger.warn("potential AdvancedAccess internal error... CompletionListener found null.");
				return;
			}
			listener.stop();
			// }
			// m_completionListeners.clear();
			m_completionListener.patternUnavailable(object2beLost);
		}
	};

	protected final PatternListener<P> m_completionListener = new PatternListener<P>() {

		@Override
		public void patternAvailable(P fulfilledDemand) {
			m_listener.patternAvailable(fulfilledDemand);  // catch exception?
			availablePatterns.add(fulfilledDemand.model.getPath());
		}

		@Override
		public void patternUnavailable(P object2beLost) {
			if (availablePatterns.remove(object2beLost.model.getPath()))
				m_listener.patternUnavailable(object2beLost); // catch exception?
		}
	};

	@Override
	public List<P> getIncompletePatterns() {
		List<P> unfulfilleds = new ArrayList<>();
		for (Map.Entry<String, CompletionListener<P>> primaryModel : m_completionListeners
				.entrySet()) {
			if (!availablePatterns.contains(primaryModel.getKey()))
				unfulfilleds.add(primaryModel.getValue().m_rad);
		}
		return unfulfilleds;
	}

	@Override
	public List<P> getCompletedPatterns() {
		List<P> completed = new ArrayList<>();
		for (Map.Entry<String, CompletionListener<P>> primaryModel : m_completionListeners
				.entrySet()) {
			if (availablePatterns.contains(primaryModel.getKey()))
				completed.add(primaryModel.getValue().m_rad);
		}
		return completed;
	}

	// TODO filter by resoure read permissions!
	@Override
	public List<PatternCondition> getConditions(ResourcePattern<?> pattern) {
		if (pattern == null)
			throw new NullPointerException("Null arguments not allowed");
		else if (!m_radClass.isAssignableFrom(pattern.getClass()))
			throw new IllegalArgumentException("Only accept patterns of type "
					+ m_radClass.getName() + "; got instead "
					+ pattern.getClass().getName());
		CompletionListener<P> cl = m_completionListeners.get(pattern.model
				.getPath());
		if (cl == null) {
			cl = m_completionListeners.get(pattern.model.getLocation()); // FIXME can this happen?
			if (cl == null)
				throw new RuntimeException("Pattern unexpectedly not found");
		}
		return new ArrayList<PatternCondition>(cl.getAllConnectedResources());
	}

	@Override
	public AdminApplication getApplication() {
		// requires admin permission
		if (System.getSecurityManager() != null
				&& !m_permMan.handleSecurity(new AdminPermission(AdminPermission.APP)))
			throw new SecurityException("Operation requires application administration permission"); 
		AdministrationManager admin = m_appMan.getAdministrationManager();
		String thisAppId = m_appMan.getAppID().getIDString();
		return admin.getAppById(thisAppId);
	}

	@Override
	public Class<? extends ResourcePattern<?>> getDemandedPatternType() {
		return m_radClass;
	}

	@Override
	public PatternListener<?> getListener() {
		return m_listener;
	}

	@Override
	public Class<? extends Resource> getPatternDemandedModelType() {
		return m_demandedModelType;
	}

}
