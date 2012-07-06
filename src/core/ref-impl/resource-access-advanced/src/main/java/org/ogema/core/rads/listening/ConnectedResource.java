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

import org.ogema.core.model.Resource;
import org.ogema.core.rads.tools.ResourceFieldInfo;
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.core.resourcemanager.AccessModeListener;
import org.ogema.core.resourcemanager.ResourceListener;
import org.ogema.core.resourcemanager.ResourceStructureEvent;
import org.ogema.core.resourcemanager.ResourceStructureEvent.EventType;
import org.ogema.core.resourcemanager.ResourceStructureListener;
import org.ogema.core.resourcemanager.pattern.ResourcePattern.CreateMode;

/**
 * Class that listens to the existence of a resource and reports changes back to
 * the CompletionListener
 */
@SuppressWarnings("rawtypes")
class ConnectedResource {

	private final Resource m_resource;
	private final CompletionListener m_listener;
	private final ResourceFieldInfo m_info;

	private boolean m_complete = false;

	public ConnectedResource(Resource resource, ResourceFieldInfo info, CompletionListener listener) {
		m_resource = resource;
		m_info = info;
		m_listener = listener;
	}

	public void start() {
		m_resource.addStructureListener(structureListener);
		m_resource.addAccessModeListener(accessListener);
		if (m_info.isEqualityRequired()) {
			m_resource.addResourceListener(m_valueListener, false);
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
		m_resource.removeStructureListener(structureListener);
		m_resource.removeAccessModeListener(accessListener);
		if (m_info.isEqualityRequired()) {
			m_resource.removeResourceListener(m_valueListener);
		}
	}

	private void recheckCompletion() {
		if (m_complete) {
			if (this.meetsRequirements())
				return;
			m_complete = false;
			m_listener.resourceUnavailable(this, !m_resource.exists());
		}
		else {
			if (!this.meetsRequirements())
				return;
			m_complete = true;
			m_listener.resourceAvailable(this);
		}
	}

	private final AccessModeListener accessListener = new AccessModeListener() {

		@Override
		public void accessModeChanged(Resource resource) {
			if (!resource.equalsPath(m_resource)) { // sanity check.
				if (resource.equalsLocation(m_resource)) {
					//					System.err.println("Got an accessModeChanged callback on correct resource location="
					//							+ resource.getLocation() + " but incorrect path=" + resource.getPath()
					//							+ " (should be path=" + m_resource.getPath()
					//							+ "): This should probably not happen. Continue and hope for the best.");
					throw new RuntimeException("Got an accessModeChanged callback on correct resource location="
							+ resource.getLocation() + " but incorrect path=" + resource.getPath()
							+ " (should be path=" + m_resource.getPath() + "): This should probably not happen.");
				}
				else {
					throw new RuntimeException("Got accessModeChanged callback for wrong resource at loation "
							+ resource.getLocation() + ". Expected resource at " + m_resource.getLocation());
				}
			}
			ConnectedResource.this.recheckCompletion();
		}
	};

	/**
	 * Listener in the resource values in case that an @Equals annotation had
	 * been issued in the RAD.
	 */
	private final ResourceListener m_valueListener = new ResourceListener() {

		@Override
		public void resourceChanged(Resource resource) {
			ConnectedResource.this.recheckCompletion();
		}
	};

	/**
	 * Listener to changes of the resource (activation, deactivation, deletion,
	 * creation)
	 */
	private final ResourceStructureListener structureListener = new ResourceStructureListener() {

		@Override
		public void resourceStructureChanged(ResourceStructureEvent event) {
			final EventType eventType = event.getType();
			//			System.out.printf("ConnectedResource(complete=%b)-- %s %s %s%n", m_complete, event.getSource().getPath(),
			//					event.getType(), event.getChangedResource());
			if (eventType == EventType.SUBRESOURCE_ADDED || eventType == EventType.SUBRESOURCE_REMOVED) {
				return;
			}
			ConnectedResource.this.recheckCompletion();
			//			System.out.printf("complete: %b%n", m_complete);
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

		if (m_info.getCreateMode() == CreateMode.MUST_EXIST) {
			if (!m_resource.isActive())
				return false;
		}

		// Check the access mode.
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

		if (m_info.isEqualityRequired()) {
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
}
