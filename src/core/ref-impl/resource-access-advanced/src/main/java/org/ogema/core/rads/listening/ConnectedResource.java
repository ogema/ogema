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

import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.Resource;
import org.ogema.core.rads.tools.ResourceFieldInfo;
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.core.resourcemanager.AccessModeListener;
import org.ogema.core.resourcemanager.ResourceStructureEvent;
import org.ogema.core.resourcemanager.ResourceStructureEvent.EventType;
import org.ogema.core.resourcemanager.ResourceStructureListener;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.core.resourcemanager.pattern.ResourcePattern.CreateMode;

/**
 * Class that listens to the existence of a resource and reports changes back to
 * the CompletionListener
 */
@SuppressWarnings("rawtypes")
class ConnectedResource {

	protected final Resource m_resource;
	private final CompletionListener m_listener;
	private final ResourceFieldInfo m_info;
	private final ResourceValueListener<Resource> initValueListener;
	private final OgemaLogger logger;
	private boolean equalsAnnotation = false;
	private boolean listenValue = false;
	private boolean valueListenerActive = false;

	private boolean m_complete = false;

	private boolean structureListenerActive = false;
	private boolean accessModeListenerActive = false;

	public ConnectedResource(Resource resource, ResourceFieldInfo info, final CompletionListener listener,
			final OgemaLogger logger) {
		m_resource = resource;
		m_info = info;
		m_listener = listener;
		this.logger = logger;
		// this listener is used to check both @Equals and @ValueChangedListener annotations
		initValueListener = new ResourceValueListener<Resource>() {

			@Override
			public void resourceChanged(Resource resource) {
				if (!valueListenerActive) {
					logger.warn("Value changed callback although listener has been deregistered");
					return;
				}
				boolean complete_bak = m_complete;
				if (equalsAnnotation) {
					ConnectedResource.this.recheckCompletion();
					if (m_complete != complete_bak) {
						if (m_complete)
							m_listener.resourceAvailable(ConnectedResource.this);
						else
							m_listener.resourceUnavailable(ConnectedResource.this, !m_resource.exists());
					}
				}
				if (listenValue && complete_bak == m_complete) {
					listener.valueChanged(ConnectedResource.this);
				}
			}
		};
	}

	public void start() {
		structureListenerActive = true;
		m_resource.addStructureListener(structureListener);
		accessModeListenerActive = true;
		m_resource.addAccessModeListener(accessListener);
		if (m_info.isEqualityRequired()) {
			equalsAnnotation = true;
			//System.out.println("   Adding value listener " + m_resource.getLocation());
			//m_resource.addValueListener(valueListener);
			m_resource.addValueListener(initValueListener);
			valueListenerActive = true;
		}
		m_resource.requestAccessMode(m_info.getMode(), m_info.getPrio());
		if (this.meetsRequirements()) {
			m_complete = true;
			m_listener.resourceAvailable(this);
		}
		else {
			m_complete = false;
		}
	}

	public void stop() {
		m_complete = false;
		structureListenerActive = false;
		m_resource.removeStructureListener(structureListener);
		accessModeListenerActive = false;
		m_resource.removeAccessModeListener(accessListener);
		if (m_info.isEqualityRequired()) {
			//System.out.println("   Removing value listener " + m_resource.getLocation());
			//m_resource.removeValueListener(valueListener);
			m_resource.removeValueListener(initValueListener);
		}
		stopValueListener();
	}

	public void startValueListener() {
		//stopValueListener(); // make sure the listener is not registered twice
		//m_resource.addValueListener(initValueListener);
		listenValue = true;
		if (!valueListenerActive) {
			m_resource.addValueListener(initValueListener);
			valueListenerActive = true;
		}
	}

	public void stopValueListener() {
		//m_resource.removeValueListener(initValueListener);
		listenValue = false;
		if (!equalsAnnotation) {
			m_resource.removeValueListener(initValueListener);
			valueListenerActive = false;
		}
	}

	private void recheckCompletion() {
		if (m_complete) {
			if (this.meetsRequirements())
				return;
			m_complete = false;
			//m_listener.resourceUnavailable(this, !m_resource.exists()); // callbacks now issued in resourceStructureChanged method
		}
		else {
			if (!this.meetsRequirements())
				return;
			m_complete = true;
			//m_listener.resourceAvailable(this);
		}
	}

	private final AccessModeListener accessListener = new AccessModeListener() {

		@Override
		public void accessModeChanged(Resource resource) {
			if (!accessModeListenerActive) {
				logger.warn("AccessMode callback although listener has been deregistered");
				return;
			}
			if (!resource.equalsPath(m_resource)) { // sanity check.
				if (resource.equalsLocation(m_resource)) {
					throw new RuntimeException("Got an accessModeChanged callback on correct resource location="
							+ resource.getLocation() + " but incorrect path=" + resource.getPath()
							+ " (should be path=" + m_resource.getPath() + "): This should probably not happen.");
				}
				else {
					throw new RuntimeException("Got accessModeChanged callback for wrong resource at loation "
							+ resource.getLocation() + ". Expected resource at " + m_resource.getLocation());
				}
			}
			boolean complete_bak = m_complete;
			ConnectedResource.this.recheckCompletion();
			if (complete_bak == m_complete)
				return;
			else if (m_complete)
				m_listener.resourceAvailable(ConnectedResource.this);
			else
				m_listener.resourceUnavailable(ConnectedResource.this, !m_resource.exists());
		}
	};

	/**
	 * Listener to changes of the resource (activation, deactivation, deletion,
	 * creation)
	 */
	private final ResourceStructureListener structureListener = new ResourceStructureListener() {

		@Override
		public void resourceStructureChanged(ResourceStructureEvent event) {
			if (!structureListenerActive) {
				logger.warn("Structure callback received although listener has been deregistered");
				return;
			}

			final EventType eventType = event.getType();
			if (eventType == EventType.SUBRESOURCE_ADDED || eventType == EventType.SUBRESOURCE_REMOVED) {
				return;
			}

			ConnectedResource.this.recheckCompletion();
			if (eventType == EventType.RESOURCE_DELETED || eventType == EventType.RESOURCE_DEACTIVATED) {
				m_listener.resourceUnavailable(ConnectedResource.this, !m_resource.exists());
			}
			// FIXME: the latter condition can occur if a resource is added as a reference... 
			else if (eventType == EventType.RESOURCE_ACTIVATED
					|| (m_resource.isActive() && (eventType == EventType.REFERENCE_ADDED || eventType == EventType.RESOURCE_CREATED))) {
				m_listener.resourceAvailable(ConnectedResource.this);
			}
		}
	};

	/*
	 * Tells if the current state of the resource satisfies the requirements set
	 * by the ResourceFieldInfo.
	 */
	private boolean meetsRequirements() {
		if (m_resource == null) {
			throw new RuntimeException("Resource is null. Programmer is not convinced this should ever happen.");
		}

		// Check existence, if required
		if (m_info.getCreateMode() == CreateMode.MUST_EXIST) {
			if (!m_resource.isActive())
				return false;
		}

		// Check the access mode, if required
		if (m_info.isAccessModeRequired()) {
			final AccessMode access = m_resource.getAccessMode();
			final AccessMode accessReq = m_info.getMode();
			if (accessReq == AccessMode.SHARED) {
				if (access == AccessMode.READ_ONLY)
					return false;
			}
			else if (accessReq == AccessMode.EXCLUSIVE) {
				if (access != AccessMode.EXCLUSIVE)
					return false;
			}
		}

		// Check equality, if required
		if (m_resource.isActive() && m_info.isEqualityRequired()) {
			if (!m_info.valueSatisfied(m_resource))
				return false;
		}

		return true;
	}

	public final boolean isComplete() {
		return m_complete;
	}

	public final boolean isRequired() {
		return (m_info.getCreateMode() == CreateMode.MUST_EXIST);
	}

	public final boolean requiresValueListener() {
		return m_info.requiresValueListener();
	}

}
