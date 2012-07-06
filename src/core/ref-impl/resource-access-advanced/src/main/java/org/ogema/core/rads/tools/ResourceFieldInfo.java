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
import org.ogema.core.resourcemanager.pattern.ResourcePattern.CreateMode;
import org.ogema.core.resourcemanager.pattern.ResourcePattern.Existence;

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

		//		final Equals equalsRequired = field.getAnnotation(Equals.class);
		//		if (equalsRequired != null) {
		//			m_valueRequired = true;
		//			m_requiredValue = equalsRequired.value();
		//		}
		//		else {
		//			m_valueRequired = false;
		//			m_requiredValue = 0;
		//		}
		// XX remnants of the @Equals annotation
		m_valueRequired = false;
		m_requiredValue = 0;
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
