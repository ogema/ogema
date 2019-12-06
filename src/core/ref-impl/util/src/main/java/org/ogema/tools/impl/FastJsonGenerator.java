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
package org.ogema.tools.impl;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.List;

import org.ogema.core.channelmanager.measurements.BooleanValue;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.IntegerValue;
import org.ogema.core.channelmanager.measurements.LongValue;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.StringValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.model.Resource;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.model.units.PhysicalUnit;
import org.ogema.core.model.units.PhysicalUnitResource;
import org.ogema.core.tools.SerializationManager;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ogema.core.model.array.ArrayResource;
import org.ogema.core.model.array.BooleanArrayResource;
import org.ogema.core.model.array.ByteArrayResource;
import org.ogema.core.model.array.FloatArrayResource;
import org.ogema.core.model.array.IntegerArrayResource;
import org.ogema.core.model.array.StringArrayResource;
import org.ogema.core.model.array.TimeArrayResource;

/**
 *
 * @author Eric Sternberg (esternberg)
 * @deprecated not suited for extensions of the REST interface; should offer static methods 
 * to serialize resources without header, etc. To be replaced.
 */
@Deprecated 
public class FastJsonGenerator {

	final static JsonFactory JSONFACTORY = new JsonFactory();
	private JsonGenerator jGen = null;
	private StateController stateControl = null;

	/**
	 *
	 * @param resource
	 * @param serializationManager
	 * @return String of serialized json limited by options, setup in serializationManager
	 * @throws IOException
	 */
	public String serialize(Resource resource, SerializationManager serializationManager) throws IOException {
		final StringWriter writer = new StringWriter();
		serialize(writer, resource, serializationManager);
		// writer.flush();
		return writer.toString();
	}

	/**
	 *
	 * @param writer
	 * @param resource
	 * @param serializationManager
	 * @throws IOException
	 */
	public void serialize(Writer writer, Resource resource, SerializationManager serializationManager)
			throws IOException {
		jGen = createJsonGenerator(writer);
		this.stateControl = new StateController(serializationManager, resource); 
    	this.serializeResource(resource);
		jGen.flush();
	}

	/**
	 *
	 * @param writer
	 * @param obj
	 * @param serializationManager
	 * @throws IOException
	 */
	/* FIXME?: this will always serialize embedded schedules (timeseries) as link */
	public void serialize(Writer writer, Object obj, SerializationManager serializationManager) throws IOException {
		ObjectMapper mapper = SerializationCore.createJacksonMapper(true);
		jGen = createJsonGenerator(writer).setCodec(mapper);
		jGen.writeObject(obj);
		jGen.flush();
	}

	/**
	 * Beginning of the real serialization
	 *
	 * @param resource
	 * @throws IOException
	 */
	private void serializeResource(Resource resource) throws IOException {
		if (stateControl.doDescent(resource)) {
			// true: unknown resource, serialize full and recursively proceed
			this.serializeFullResource(resource);
		}
		else {
			this.serializeResourceAsLink(resource);
		}
		stateControl.decreaseDepth();
	}

	/**
	 * Serializes all resource-fields and subresources
	 *
	 * @param resource
	 * @throws IOException
	 */
	private void serializeFullResource(Resource resource) throws IOException {
		// begin serialize Resource
		jGen.writeStartObject();

		// the resource type occures as "@type" element just in the requested root-resource
		if (stateControl.isRquestedRootResource(resource)) {
			jGen.writeStringField("@type", this.figureOutResourceType(resource));
			this.writeResourceBody(resource);
		}
		else {
			jGen.writeObjectFieldStart(this.figureOutResourceType(resource));
			this.writeResourceBody(resource);
			jGen.writeEndObject();
		}

		// end serialize Resource
		jGen.writeEndObject();
	}

	/**
	 * Writes all fields of an resource, it calls serializeSubResources(resource) to rekursively create json for each
	 * subresource
	 *
	 * @param resource
	 * @throws IOException
	 */
	private void writeResourceBody(Resource resource) throws IOException {
		this.writeSimpleResourceValue(resource);
		jGen.writeStringField("name", resource.getName());
		jGen.writeStringField("type", resource.getResourceType().getCanonicalName());
		jGen.writeStringField("path", resource.getPath());
		jGen.writeBooleanField("decorating", resource.isDecorator());
		jGen.writeBooleanField("active", resource.isActive());
		jGen.writeBooleanField("referencing", resource.isReference(true));
		if (resource instanceof ResourceList) {
			@SuppressWarnings("unchecked")
			Class<? extends Resource> listType = ((ResourceList) resource).getElementType();
			jGen.writeStringField("elementType", listType != null ? listType.getName() : null);
		}
		// NOTE: There is no Type of Schedule in Ogema api, that contains all xsd-dfined values
		if (resource instanceof Schedule) {
			this.serializeEntrys((Schedule) resource);
		}
		// serialize subresources
		this.serializeSubResources(resource);
	}

	/**
	 * determines the type of the given Resource and if its some kind of simpe resource, it writes the corresponding
	 * value-field to json, else it does nothing
	 *
	 * @param resource
	 * @throws IOException
	 */
	private void writeSimpleResourceValue(Resource resource) throws IOException {
		if (!(resource instanceof SingleValueResource || resource instanceof ArrayResource)) {
			return;
		}
		if (resource instanceof BooleanResource) {
			jGen.writeBooleanField(SINGLEVALUE, ((BooleanResource) resource).getValue());
		}
		else if (resource instanceof FloatResource) {
			jGen.writeNumberField(SINGLEVALUE, ((FloatResource) resource).getValue());
			if (resource instanceof PhysicalUnitResource) {
				PhysicalUnit u = ((PhysicalUnitResource) resource).getUnit();
				if (u != null) {
					jGen.writeStringField("unit", u.toString());
				}
			}
		}
		else if (resource instanceof IntegerResource) {
			jGen.writeNumberField(SINGLEVALUE, ((IntegerResource) resource).getValue());
		}
		else if (resource instanceof TimeResource) {
			jGen.writeNumberField(SINGLEVALUE, ((TimeResource) resource).getValue());
		}
		else if (resource instanceof StringResource) {
			jGen.writeStringField(SINGLEVALUE, ((StringResource) resource).getValue());
		}
		else if (resource instanceof org.ogema.core.model.simple.OpaqueResource) {
			// Jackson default Base64 variant (which is Base64Variants.MIME_NO_LINEFEEDS)
			jGen.writeBinaryField(SINGLEVALUE, ((org.ogema.core.model.simple.OpaqueResource) resource).getValue());
		} else if (resource instanceof ArrayResource) {
            if (resource instanceof BooleanArrayResource) {
                BooleanArrayResource arr = (BooleanArrayResource) resource;
                jGen.writeArrayFieldStart(ARRAYVALUES);
                for (boolean b: arr.getValues()) {
                    jGen.writeBoolean(b);
                }
                jGen.writeEndArray();
            } else if (resource instanceof ByteArrayResource) {
                ByteArrayResource arr = (ByteArrayResource) resource;
                jGen.writeBinaryField(ARRAYVALUES, arr.getValues());
            } else if (resource instanceof FloatArrayResource) {
                FloatArrayResource arr = (FloatArrayResource) resource;
                jGen.writeArrayFieldStart(ARRAYVALUES);
                for (float f: arr.getValues()) {
                    jGen.writeNumber(f);
                }
                jGen.writeEndArray();
            } else if (resource instanceof IntegerArrayResource) {
                IntegerArrayResource arr = (IntegerArrayResource) resource;
                jGen.writeArrayFieldStart(ARRAYVALUES);
                for (int i: arr.getValues()) {
                    jGen.writeNumber(i);
                }
                jGen.writeEndArray();
            } else if (resource instanceof StringArrayResource) {
                StringArrayResource arr = (StringArrayResource) resource;
                jGen.writeArrayFieldStart(ARRAYVALUES);
                for (String s: arr.getValues()) {
                    jGen.writeString(s);
                }
                jGen.writeEndArray();
            } else if (resource instanceof TimeArrayResource) {
                TimeArrayResource arr = (TimeArrayResource) resource;
                jGen.writeArrayFieldStart(ARRAYVALUES);
                for (long l: arr.getValues()) {
                    jGen.writeNumber(l);
                }
                jGen.writeEndArray();
            } else {
                LoggerFactory.getLogger(getClass()).error(
                        "unsupported ArrayResource type: {}", resource.getResourceType());
            }
        }
	}
    private static final String ARRAYVALUES = "values";
    private static final String SINGLEVALUE = "value";

	/**
	 * Just writes the fields to json needed for a resource-link. No further rekursive calls are done.
	 *
	 * @param resource
	 * @throws IOException
	 */
	private void serializeResourceAsLink(Resource resource) throws IOException {
		jGen.writeStartObject();
		jGen.writeObjectFieldStart("resourcelink");
		jGen.writeStringField("link", resource.getLocation());
		jGen.writeStringField("type", resource.getResourceType().getCanonicalName());
		jGen.writeStringField("name", resource.getName());
		jGen.writeEndObject();
		jGen.writeEndObject();
	}

	/**
	 * Recursively creates json for all subresources by calling this.serializeResource(subRes)
	 *
	 * @param resource
	 * @throws IOException
	 */
	private void serializeSubResources(Resource resource) throws IOException {
		jGen.writeArrayFieldStart("subresources");
		// rekursivly write out the subresources, important to get them non-rekursive
		for (Resource subRes : resource.getSubResources(false)) {
			this.serializeResource(subRes);
		}
		jGen.writeEndArray();
	}

	private void serializeEntrys(Schedule definitionSchedule) throws IOException {
		serializeEntrys(definitionSchedule, 0, Long.MAX_VALUE);
	}

	@SuppressWarnings("deprecation")
	private void serializeEntrys(Schedule definitionSchedule, long start, long end) throws IOException {
		if (definitionSchedule.getTimeOfLatestEntry() != null) {
			jGen.writeNumberField("lastUpdateTime", definitionSchedule.getTimeOfLatestEntry());
		}
		if (definitionSchedule.getLastCalculationTime() != null) {
			jGen.writeNumberField("lastCalculationTime", definitionSchedule.getLastCalculationTime());
		}
		List<SampledValue> entries = definitionSchedule.getValues(start, end);
		jGen.writeNumberField("start", start);
		jGen.writeNumberField("end", end);
		jGen.writeArrayFieldStart("entry");
		// write sampled values
		if (!entries.isEmpty()) {
			SampledValuesWriter w = SampledValuesWriter.forValue(entries.get(0));
			for (SampledValue sampledValue : entries) {
				w.write(sampledValue, jGen);
			}
		}
		jGen.writeEndArray();
	}

	enum SampledValuesWriter {

		BooleanWriter(BooleanValue.class) {
			@Override
			public void writeValue(SampledValue v, JsonGenerator jGen) throws IOException {
				jGen.writeBooleanField(SINGLEVALUE, v.getValue().getBooleanValue());
				jGen.writeStringField("@type", "SampledBoolean");
			}
		},
		FloatWriter(FloatValue.class) {
			@Override
			public void writeValue(SampledValue v, JsonGenerator jGen) throws IOException {
				jGen.writeNumberField(SINGLEVALUE, v.getValue().getFloatValue());
				jGen.writeStringField("@type", "SampledFloat");
			}
		},
		IntWriter(IntegerValue.class) {
			@Override
			public void writeValue(SampledValue v, JsonGenerator jGen) throws IOException {
				jGen.writeNumberField(SINGLEVALUE, v.getValue().getIntegerValue());
				jGen.writeStringField("@type", "SampledInteger");
			}
		},
		LongWriter(LongValue.class) {
			@Override
			public void writeValue(SampledValue v, JsonGenerator jGen) throws IOException {
				jGen.writeNumberField(SINGLEVALUE, v.getValue().getLongValue());
				jGen.writeStringField("@type", "SampledLong");
			}
		},
		StringWriter(StringValue.class) {
			@Override
			public void writeValue(SampledValue v, JsonGenerator jGen) throws IOException {
				jGen.writeStringField(SINGLEVALUE, v.getValue().getStringValue());
				jGen.writeStringField("@type", "SampledString");
			}
		},
		DefaultWriter(Value.class) {
			@Override
			public void writeValue(SampledValue v, JsonGenerator jGen) throws IOException {
				jGen.writeStringField(SINGLEVALUE, v.getValue().getStringValue());
				jGen.writeStringField("@type", "SampledString");
			}
		};

		Class<? extends Value> type;

		SampledValuesWriter(Class<? extends Value> type) {
			this.type = type;
		}

		public static SampledValuesWriter forValue(SampledValue v) {
			Class<?> valueType = v.getValue().getClass();
			for (SampledValuesWriter writer : values()) {
				if (valueType.isAssignableFrom(writer.type)) {
					return writer;
				}
			}
			return DefaultWriter;
		}

		// FIXME this misses the type of the field... leads to failure
		void write(SampledValue val, JsonGenerator jGen) throws IOException {
			jGen.writeStartObject();
			jGen.writeNumberField("time", val.getTimestamp());
			jGen.writeStringField("quality", val.getQuality().name());
			writeValue(val, jGen);
			jGen.writeEndObject();
		}

		protected abstract void writeValue(SampledValue v, JsonGenerator jGen) throws IOException;
	}

	/**
	 * Return the value for the json field "type". If its some kind of simple resource it will return the classname of
	 * that resource (e.g. BooleanResource), else it will return "Resource"
	 *
	 * @param resource
	 * @return String
	 */
	private String figureOutResourceType(Resource resource) {

		if (resource instanceof SingleValueResource || resource instanceof ResourceList) {
			if (resource instanceof FloatResource) {
				return FloatResource.class.getSimpleName();
			}
			return resource.getResourceType().getSimpleName();
		}
		if (resource instanceof Schedule) {
			return this.figureOutScheduleType((Schedule) resource);
		}
		// return "Resource";
		// TODO: clarify why different cases in old json-serialization?
		return stateControl.isRquestedRootResource(resource) ? "Resource" : "resource";
	}

	/**
	 * We have to figgure out which (simple)type of Schedule we have ( (but there are no simple typs of schedules in the
	 * ogema model) otherwise deserialization of a schedule wont work.
	 *
	 * @param schedule
	 * @return
	 */
	private String figureOutScheduleType(Schedule schedule) {
		final Resource parent = schedule.getParent();
		if (parent instanceof BooleanResource) {
			return "BooleanSchedule";
		}
		if (parent instanceof FloatResource) {
			return "FloatSchedule";
		}
		if (parent instanceof IntegerResource) {
			return "IntegerSchedule";
		}
		if (parent instanceof org.ogema.core.model.simple.StringResource) {
			return "StringSchedule";
		}
		if (parent instanceof org.ogema.core.model.simple.TimeResource) {
			return "TimeSchedule";
		}
		// NOTE deserialization of type shedule wont work
		return "Schedule";
	}
	
	static final JsonGenerator createJsonGenerator(final Writer writer) throws IOException {
		try {
			return AccessController.doPrivileged(new PrivilegedExceptionAction<JsonGenerator>() {

				@Override
				public JsonGenerator run() throws Exception { // method accesses a system property internally
					return JSONFACTORY.createJsonGenerator(writer).useDefaultPrettyPrinter();
				}
			});
		} catch (PrivilegedActionException e) {
			final Throwable t = e.getCause();
			if (t instanceof IOException)
				throw (IOException) t;
			else if (t instanceof RuntimeException)
				throw (RuntimeException) t;
			else if (t instanceof Error)
				throw (Error) t;
			throw new RuntimeException(t);
		}
		
	}
		
}
