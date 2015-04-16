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
import java.util.List;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.Resource;
import org.ogema.core.rads.tools.RadFactory;
import org.ogema.core.rads.tools.ResourceFieldInfo;
import org.ogema.core.resourcemanager.pattern.PatternListener;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.core.resourcemanager.pattern.ResourcePattern.CreateMode;

/**
 * Takes a RAD with a matched primary demand and checks if all required fields are set.
 */
class CompletionListener<P extends ResourcePattern<?>> {

	public static final boolean debug = true;
	public static final boolean tryFixLists = false;

	private final ApplicationManager m_appMan;
	private final OgemaLogger m_logger;
	private final P m_rad;
	// List of required fields
	private final List<ConnectedResource> m_required = new ArrayList<>();
	// List of optional fields
	private final List<ConnectedResource> m_optional = new ArrayList<>();
	// List of fields that are to be created
	// private final List<ConnectedResource> m_create = new ArrayList<>();

	PatternListener<P> m_listener;

	private boolean m_completed = false;

	/*
	 * Gets all the fields annotated in the RAD, irrespective of their existence requirements.
	 */
	private List<ConnectedResource> getAllConnectedResources() {
		final List<ConnectedResource> result = new ArrayList<>(m_required.size() + m_optional.size());
		result.addAll(m_required);
		result.addAll(m_optional);
		// result.addAll(m_create);
		return result;
	}

	public CompletionListener(ApplicationManager appMan, P rad, final List<ResourceFieldInfo> fields) {
		m_appMan = appMan;
		m_logger = appMan.getLogger();
		m_rad = rad;
		for (ResourceFieldInfo info : fields) {
			final Resource resource = RadFactory.getResource(info.getField(), rad);
			final CreateMode mode = info.getCreateMode();
			if (mode == CreateMode.MUST_EXIST) {
				m_required.add(new ConnectedResource(resource, info, this));
			}
			else if (mode == CreateMode.OPTIONAL) {
				m_optional.add(new ConnectedResource(resource, info, this));
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
	}

	public void stop() {
    	//System.out.println("    Stopping completion listener " + m_rad.model.getLocation());
		for (ConnectedResource conRes : getAllConnectedResources()) {
			conRes.stop();
		}
		m_completed = false;
	}

	private void checkCompletion() {
		for (ConnectedResource conRes : m_required) {
			if (!conRes.isComplete()) {
				m_completed = false;
				return;
			}
		}

		m_completed = true;
		m_logger.debug("Completed a RAD of type " + m_rad.getClass().getCanonicalName() + " with primary demand "
				+ ((Resource) m_rad.model).getLocation());
		m_listener.patternAvailable(m_rad);
	}

	public void resourceAvailable(ConnectedResource conRes) {
		if (conRes.isRequired() && !m_completed)
			checkCompletion();
	}

	public void resourceUnavailable(ConnectedResource conRes, boolean isDeleted) {
		if (!conRes.isRequired())
			return;
		m_completed = false;
		m_listener.patternUnavailable(m_rad);
	}
}
