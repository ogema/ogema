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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.core.rads.tools.ContainerTool;
import org.ogema.core.rads.tools.RadFactory;
import org.ogema.core.rads.tools.ResourceFieldInfo;
import org.ogema.core.resourcemanager.pattern.PatternListener;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.core.resourcemanager.pattern.ResourcePattern.CreateMode;
import org.ogema.core.resourcemanager.pattern.ContextSensitivePattern;
import org.slf4j.Logger;

/**
 * Takes a RAD with a matched primary demand and checks if all required fields are set.
 */
class CompletionListener<P extends ResourcePattern<?>>  {

	public static final boolean debug = true;
	public static final boolean tryFixLists = false;

	private final ApplicationManager m_appMan;
	private final Logger m_logger;
	public final P m_rad;
	// List of required fields
	private final List<ConnectedResource> m_required;  
	// List of optional fields
	private final List<ConnectedResource> m_optional;
	// List of fields that are to be created
	// private final List<ConnectedResource> m_create = new ArrayList<>();

	PatternListener<P> m_listener;

	// a patternAvailable callback is issued as soon as both m_completed and init_satisfied are true;
	// as soon as one of them turns false, patternUnavailable is called. 
	// !m_completed => !init_satisfied
	private boolean m_completed = false;	
	private boolean init_satisfied = false;
	private final Object m_container;

	/*
	 * Gets all the fields annotated in the RAD, irrespective of their existence requirements.
	 */
	public List<ConnectedResource> getAllConnectedResources() {
		final List<ConnectedResource> result = new ArrayList<>(m_required.size() + m_optional.size());
		result.addAll(m_required);
		result.addAll(m_optional);
		// result.addAll(m_create);
		return result;
	}
	
	public CompletionListener(ApplicationManager appMan, Logger logger, P rad, final List<ResourceFieldInfo> fields) {
		this(appMan, logger, rad, fields, null);
	}

	public CompletionListener(ApplicationManager appMan, Logger logger, P rad, final List<ResourceFieldInfo> fields, Object container) {
		m_appMan = appMan;
		m_logger = logger;
		m_rad = rad;
		m_container = container;
		m_required = new ArrayList<>(5);
		m_optional = new ArrayList<>(5);
		for (ResourceFieldInfo info : fields) {
			final Resource resource = RadFactory.getResource(info.getField(), rad);
			final CreateMode mode = info.getCreateMode();
			String name = info.getField().getName();
			if (resource == null) {
				continue;   // ignore uninitialized resources
			}
			if (mode == CreateMode.MUST_EXIST) {
				m_required.add(new ConnectedResource(resource, info, this, m_logger,name,false));
			}
			else if (mode == CreateMode.OPTIONAL) {
				m_optional.add(new ConnectedResource(resource, info, this, m_logger,name,true));
			}
			else {
				throw new RuntimeException("Unsupported create mode " + mode);
			}
		}
	}

	public void start(PatternListener<P> completionListener) {
		m_listener = completionListener;
		for (ConnectedResource conRes : getAllConnectedResources()) {
			conRes.start();
		}
		boolean empty = true;
		for (ConnectedResource cr  : getAllConnectedResources()) {
			if (!cr.isOptional()) {
				empty = false;
				break;
			}
		}
		if (empty) {
			checkInitRequirement();
		}
	}

	public void stop() {
		for (ConnectedResource conRes : getAllConnectedResources()) {
			conRes.stop();
		}
		m_completed = false;
		init_satisfied = false;
	}

	private void checkCompletion() {
		for (ConnectedResource conRes : m_required) {
			if (!conRes.isSatisfied()) {
                m_logger.trace("pattern unsatisfied: {}", conRes);
				m_completed = false;
				init_satisfied = false;
				return;
			}
		}

		m_completed = true;
		m_logger.debug("Completed a RAD of type " + m_rad.getClass().getCanonicalName() + " with primary demand "
				+ ((Resource) m_rad.model).getLocation());
		checkInitRequirement();
		for (ConnectedResource cr: getAllConnectedResources()) {
			if (cr.requiresValueListener()) {
				cr.startValueListener();
			}
		}
		
	}

	public synchronized void resourceAvailable(ConnectedResource conRes) {
        m_logger.trace("connected resource available: {}", conRes);
		if (conRes.isRequired() && !m_completed) {
            checkCompletion();
        }
	}

	public synchronized void resourceUnavailable(ConnectedResource conRes, boolean isDeleted) {		
		//System.out.println("  res unavailable callback " + conRes.toString());
        m_logger.trace("connected resource unavailable: {}, deleted={}", conRes, isDeleted);
        m_logger.trace("pattern state: completed={}, init satisfied={}", m_completed, init_satisfied);
		if (!conRes.isRequired()) {
			if (m_completed) {
                checkInitRequirement();
            }
			return;
		}
		if (m_completed && init_satisfied) {
            reportPatternUnavailable(m_rad);
		}
		m_completed = false;	
		init_satisfied = false;
		for (ConnectedResource cr: getAllConnectedResources()) {
			cr.stopValueListener();
		}
		
	}
	
	public boolean isComplete() {
		return m_completed;
	}
	
	public boolean isSatisfied() {
		return init_satisfied;
	}

	public void valueChanged(ConnectedResource cr) {
		if (!m_completed) {
			throw new RuntimeException("Received a value changed callback, although the pattern structure is not complete");
		}
		checkInitRequirement();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void checkInitRequirement() {
		if (m_container != null && m_rad instanceof ContextSensitivePattern) {
			try {
				ContainerTool.setContainer((ContextSensitivePattern) m_rad, m_container);
			} catch (NoSuchFieldException | IllegalAccessException | RuntimeException e) {
				m_logger.error("Internal error: could not set pattern container for {}: " + e, m_rad);
			}
//			((ResourcePatternExtended) m_rad).setContainer(m_container);
		}
		boolean result = false;
		try {
			result = m_rad.accept();
		} catch (Exception e) {
			m_logger.error("Pattern accept method of {} has thrown an exception.", m_rad, e);
		}
		if (result) {
			if (init_satisfied) return;
			init_satisfied = true;
			//m_listener.patternAvailable(m_rad);
            reportPatternAvailable(m_rad);
		}
		else {
			if (!init_satisfied) return;
			init_satisfied = false;
			//m_listener.patternUnavailable(m_rad);
            reportPatternUnavailable(m_rad);
		}
	}
    
    private void reportPatternAvailable(final P pattern) {
        m_appMan.submitEvent(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                m_listener.patternAvailable(pattern);
                return null;
            }

        }
        );
    }
    
    private void reportPatternUnavailable(final P pattern) {
        m_appMan.submitEvent(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                m_listener.patternUnavailable(pattern);
                return null;
            }

        }
        );
    }
	
}
