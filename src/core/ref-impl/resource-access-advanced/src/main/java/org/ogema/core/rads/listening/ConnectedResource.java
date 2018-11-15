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

import org.ogema.core.administration.PatternCondition;
import org.ogema.core.model.Resource;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.rads.tools.ResourceFieldInfo;
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.core.resourcemanager.AccessModeListener;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.ResourceStructureEvent;
import org.ogema.core.resourcemanager.ResourceStructureEvent.EventType;
import org.ogema.core.resourcemanager.ResourceStructureListener;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.core.resourcemanager.pattern.ResourcePattern.CreateMode;
import org.slf4j.Logger;

/**
 * Class that listens to the existence of a resource and reports changes back to
 * the CompletionListener
 */
@SuppressWarnings("rawtypes")
class ConnectedResource implements PatternCondition {

	protected final Resource m_resource;
	private final CompletionListener m_listener;
	private final ResourceFieldInfo m_info;
	private final ResourceValueListener<Resource> initValueListener;
	private final Logger logger;
	private final boolean isOptional;
	private final String name;
	private boolean equalsAnnotation = false;
	private boolean listenValue = false;
	private boolean valueListenerActive = false;

	private boolean m_complete = false;

	private boolean structureListenerActive = false;
	private boolean accessModeListenerActive = false;

	public ConnectedResource(Resource resource, ResourceFieldInfo info, final CompletionListener listener,
			final Logger logger, String name, boolean isOptional) {
		m_resource = resource;
		m_info = info;
		m_listener = listener;
		this.name = name;
		this.isOptional = isOptional;
		this.logger = logger;
		// this listener is used to check both @Equals and @ValueChangedListener annotations
		initValueListener = new ResourceValueListener<Resource>() {

			@Override
			public void resourceChanged(Resource resource) {
				if (!valueListenerActive) {
					logger.debug("Value changed callback although listener has been deregistered");
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
		m_resource.requestAccessMode(AccessMode.READ_ONLY, AccessPriority.PRIO_LOWEST);
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
				// this is actually not so uncommon... presumably, we cannot fully prevent this situation due to timing issues
				logger.debug("AccessMode callback although listener has been deregistered");
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
            logger.trace("connected resource structure callback: {}", event);
			if (!structureListenerActive) {
				// this is actually not so uncommon... presumably, we cannot fully prevent this situation due to timing issues
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

	public final boolean isRequired() {
		return (m_info.getCreateMode() == CreateMode.MUST_EXIST);
	}

	public final boolean requiresValueListener() {
		return m_info.requiresValueListener();
	}
	
	@Override
	public final boolean isSatisfied() {
		return m_complete;
	}

	@Override
	public Class<? extends Resource> getResourceType() {
		return m_resource.getResourceType();
	}

	@Override
	public boolean exists() {
		return m_resource.exists();
	}

	@Override
	public boolean isActive() {
		return m_resource.isActive();
	}

	@Override
	public boolean isReference() {
		return m_resource.isReference(true);
	}

	@Override
	public String getPath() {
		return m_resource.getPath();
	}

	@Override
	public String getLocation() {
		return m_resource.getLocation();
	}

	@Override
	public Object getValue() {
		if (m_resource instanceof StringResource)
			return ((StringResource) m_resource).getValue();
		else if (m_resource instanceof FloatResource)
			return ((FloatResource) m_resource).getValue();
		else if (m_resource instanceof BooleanResource)
			return ((BooleanResource) m_resource).getValue();
		else if (m_resource instanceof IntegerResource)
			return ((IntegerResource) m_resource).getValue();
		else if (m_resource instanceof TimeResource)
			return ((TimeResource) m_resource).getValue();
		else if (m_resource instanceof Schedule) 
			return ((Schedule) m_resource).getValues(0).size() + " schedule values";
		return null;
	}

	@Override
	public boolean isOptional() {
		return isOptional;
	}

	@Override
	public AccessMode getAccessMode() {
		return m_resource.getAccessMode();
	}

	@Override
	public String getFieldName() {
		return name;
	}

    @Override
    public String toString() {
        return String.format("%s: %s (optional: %b, active: %b, required: %b)",
                getFieldName(), getPath(), isOptional(), isActive(), isRequired());
    }

}
