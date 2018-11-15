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
package org.ogema.core.rads.tools;

import java.lang.reflect.Field;

import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.pattern.ResourcePattern.Access;
import org.ogema.core.resourcemanager.pattern.ResourcePattern.ChangeListener;
import org.ogema.core.resourcemanager.pattern.ResourcePattern.CreateMode;
import org.ogema.core.resourcemanager.pattern.ResourcePattern.Existence;
import org.ogema.core.resourcemanager.pattern.ResourcePattern.ValueChangedListener;

/**
 * Informations on the requirements for a connected resource, read from the
 * annotations of the RAD.
 * 
 * @author Timo Fischer, Fraunhofer IWES
 */
public class ResourceFieldInfo {

	private final Field m_field;
	private final AccessMode m_mode;
	private final AccessPriority m_prio;
	private final boolean m_modeRequired;
	private final CreateMode m_createMode;
	private final boolean m_valueRequired;
	private final int m_requiredValue;
	private final boolean m_valueListener;
	private final boolean m_changedListener_value; // TODO use array instead?
	private final boolean m_changedListener_structure;
	private final boolean m_changedListener_callOnEveryUpdate;

	public ResourceFieldInfo(Field field, AccessPriority writePriority) {
		m_field = field;

		// Evaluate the @Access annotation to determine the access levels required.
		final Access access = field.getAnnotation(Access.class);
		if (access != null) {
			m_mode = access.mode();
			m_modeRequired = access.required();
		}
		else {
			m_mode = AccessMode.SHARED;
			m_modeRequired = false;
		}
		//XXX probably not in the right place
		if (writePriority == null) {
			writePriority = AccessPriority.PRIO_LOWEST;
		}
		m_prio = (m_mode == AccessMode.EXCLUSIVE) ? writePriority : AccessPriority.PRIO_LOWEST;

		// Evaluate the @Existence annotation to determine if the element is required.
		final Existence existence = field.getAnnotation(Existence.class);
		if (existence != null) {
			m_createMode = existence.required();
		}
		else {
			m_createMode = CreateMode.MUST_EXIST;
		}

		// No values required at the current state of the API.
		@SuppressWarnings("deprecation")
		final org.ogema.core.resourcemanager.pattern.ResourcePattern.Equals equals = field
				.getAnnotation(org.ogema.core.resourcemanager.pattern.ResourcePattern.Equals.class);
		if (equals != null) {
			m_valueRequired = true;
			m_requiredValue = equals.value();
		}
		else {
			m_valueRequired = false;
			m_requiredValue = 0;
		}

		final ValueChangedListener vcl = field.getAnnotation(ValueChangedListener.class);
		if (vcl != null) {
			m_valueListener = vcl.activate();
		}
		else {
			m_valueListener = false;
		}
		final ChangeListener cl = field.getAnnotation(ChangeListener.class);
		if (cl == null) {
			m_changedListener_value = false;
			m_changedListener_structure = false;
			m_changedListener_callOnEveryUpdate = false;
		}
		else {
			m_changedListener_value = cl.valueListener();
			m_changedListener_structure = cl.structureListener();
			m_changedListener_callOnEveryUpdate =cl.callOnEveryUpdate();
		}
	}

	public Field getField() {
		return m_field;
	}

	public AccessMode getMode() {
		return m_mode;
	}

	public AccessPriority getPrio() {
		return m_prio;
	}

	public boolean isAccessModeRequired() {
		return m_modeRequired;
	}

	public boolean isEqualityRequired() {
		return m_valueRequired;
	}

	public int getEqualsValue() {
		return m_requiredValue;
	}

	public boolean requiresValueListener() {
		return m_valueListener;
	}
	
	public boolean requiresChangeListenerValue() {
		return m_changedListener_value;
	}
	
	public boolean changeListenerValueCallOnEveryUpdate() {
		return m_changedListener_callOnEveryUpdate;
	}
	
	public boolean requiresChangeListenerStructure() {
		return m_changedListener_structure;
	}

	public boolean valueSatisfied(Resource resource) {

		if (!m_valueRequired)
			return true;

		// inactive resources are okay, but non-existing does not allow to check for equality.
		if (!resource.exists())
			return false;

		if (resource instanceof FloatResource) {
			final FloatResource fRes = (FloatResource) resource;
			return fRes.getValue() == m_requiredValue;
		}
		if (resource instanceof IntegerResource) {
			final IntegerResource fRes = (IntegerResource) resource;
			return fRes.getValue() == m_requiredValue;
		}
		if (resource instanceof TimeResource) {
			final TimeResource fRes = (TimeResource) resource;
			return fRes.getValue() == m_requiredValue;
		}
		if (resource instanceof BooleanResource) {
			final BooleanResource fRes = (BooleanResource) resource;
			if (fRes.getValue() == true) {
				return m_requiredValue == 1;
			}
			else {
				return m_requiredValue == 0;
			}
		}
		throw new UnsupportedOperationException("Cannot check @Equals annotaiton for type "
				+ resource.getResourceType().getCanonicalName()
				+ ". Either the RAD was incorrect or a relevant case has not been implemented, yet.");
	}

	public CreateMode getCreateMode() {
		return m_createMode;
	}
}
