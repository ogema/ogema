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
package org.ogema.serialization;

import org.ogema.core.model.Resource;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.SimpleResource;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.OpaqueResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.tools.SerializationManager;
import org.ogema.serialization.schedules.BooleanSchedule;
import org.ogema.serialization.schedules.FloatSchedule;
import org.ogema.serialization.schedules.IntegerSchedule;
import org.ogema.serialization.schedules.JaxbSchedule;
import org.ogema.serialization.schedules.StringSchedule;
import org.ogema.serialization.schedules.TimeSchedule;

/**
 * Factory for the JaxbResources, which can not be instanciated explicitly.
 * 
 * @author Timo Fischer, Fraunhofer IWES
 */
public class JaxbFactory {

	public static JaxbResource createJaxbResource(Resource resource, SerializationManager manager) {
		final SerializationStatus status = new SerializationStatus(manager);
		return createJaxbResource(resource, status);
	}

	public static JaxbSchedule<?> createJaxbSchedule(Schedule schedule, SerializationManager manager, long t0, long t1) {
		final SerializationStatus status = new SerializationStatus(manager);
		return createJaxbSchedule(schedule, status, t0, t1);
	}

	/**
	 * Creates a {@link JaxbResource} wrapper for an OGEMA resource.
	 */
	static JaxbResource createJaxbResource(Resource resource, SerializationStatus status) {
		if (resource instanceof SimpleResource) {
			if (resource instanceof BooleanResource) {
				return new JaxbBoolean((BooleanResource) resource, status);
			}
			if (resource instanceof FloatResource) {
				return new JaxbFloat((FloatResource) resource, status);
			}
			if (resource instanceof IntegerResource) {
				return new JaxbInteger((IntegerResource) resource, status);
			}
			if (resource instanceof OpaqueResource) {
				return new JaxbOpaque((OpaqueResource) resource, status);
			}
			if (resource instanceof StringResource) {
				return new JaxbString((StringResource) resource, status);
			}
			if (resource instanceof TimeResource) {
				return new JaxbTime((TimeResource) resource, status);
			}
		}

		if (resource instanceof ResourceList) {
			return new JaxbResourceList((ResourceList) resource, status);
		}

		if (resource instanceof Schedule) {
			Schedule schedule = (Schedule) resource;
			return createJaxbSchedule(schedule, status, 0, Long.MAX_VALUE);
		}
		return new JaxbResource(resource, status);
	}

	static JaxbSchedule<?> createJaxbSchedule(Schedule schedule, SerializationStatus status, long t0, long t1) {
		final Resource parent = schedule.getParent();
		if (parent instanceof BooleanResource) {
			return new BooleanSchedule(schedule, status, t0, t1);
		}
		if (parent instanceof FloatResource) {
			return new FloatSchedule(schedule, status, t0, t1);
		}
		if (parent instanceof IntegerResource) {
			return new IntegerSchedule(schedule, status, t0, t1);
		}
		if (parent instanceof StringResource) {
			return new StringSchedule(schedule, status, t0, t1);
		}
		if (parent instanceof TimeResource) {
			return new TimeSchedule(schedule, status, t0, t1);
		}
		throw new UnsupportedOperationException("Cannot serialize schedule: unsupported type: " + parent);
	}
}
