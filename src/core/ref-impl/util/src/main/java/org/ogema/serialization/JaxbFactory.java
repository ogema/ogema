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
package org.ogema.serialization;

import java.util.Collection;

import org.ogema.core.model.Resource;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.array.ArrayResource;
import org.ogema.core.model.array.BooleanArrayResource;
import org.ogema.core.model.array.ByteArrayResource;
import org.ogema.core.model.array.FloatArrayResource;
import org.ogema.core.model.array.IntegerArrayResource;
import org.ogema.core.model.array.StringArrayResource;
import org.ogema.core.model.array.TimeArrayResource;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.SingleValueResource;
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

	public static JaxbResourceCollection createJaxbResources(Collection<Resource> resources, SerializationManager manager) {
		final SerializationStatus status = new SerializationStatus(manager);
		return createJaxbResources(resources, status);
	}
	
	/**
	 * Creates a {@link JaxbResource} wrapper for an OGEMA resource.
	 */
	@SuppressWarnings("deprecation")
	public static JaxbResource createJaxbResource(Resource resource, SerializationStatus status) {
		if (resource instanceof SingleValueResource) {
			if (resource instanceof BooleanResource) {
				return new JaxbBoolean((BooleanResource) resource, status);
			}
			if (resource instanceof FloatResource) {
				return new JaxbFloat((FloatResource) resource, status);
			}
			if (resource instanceof IntegerResource) {
				return new JaxbInteger((IntegerResource) resource, status);
			}
			if (resource instanceof org.ogema.core.model.simple.OpaqueResource) {
				return new JaxbOpaque((org.ogema.core.model.simple.OpaqueResource) resource, status);
			}
			if (resource instanceof StringResource) {
				return new JaxbString((StringResource) resource, status);
			}
			if (resource instanceof TimeResource) {
				return new JaxbTime((TimeResource) resource, status);
			}
		}

		if (resource instanceof ArrayResource) {
			if (resource instanceof BooleanArrayResource) {
				return new JaxbBooleanArray((BooleanArrayResource) resource, status);
			}
			if (resource instanceof ByteArrayResource) {
				return new JaxbByteArray((ByteArrayResource) resource, status);
			}
			if (resource instanceof FloatArrayResource) {
				return new JaxbFloatArray((FloatArrayResource) resource, status);
			}
			if (resource instanceof IntegerArrayResource) {
				return new JaxbIntegerArray((IntegerArrayResource) resource, status);
			}
			if (resource instanceof org.ogema.core.model.simple.OpaqueResource) {
				return new JaxbOpaque((org.ogema.core.model.simple.OpaqueResource) resource, status);
			}
			if (resource instanceof StringArrayResource) {
				return new JaxbStringArray((StringArrayResource) resource, status);
			}
			if (resource instanceof TimeArrayResource) {
				return new JaxbTimeArray((TimeArrayResource) resource, status);
			}
			throw new UnsupportedOperationException("fixme: unsupported array type " + resource.getResourceType());
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
	
	private static JaxbResourceCollection createJaxbResources(Collection<Resource> resources, SerializationStatus status) {
		return new JaxbResourceCollection(resources,status);
	}
}
