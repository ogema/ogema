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
package org.ogema.tools.impl;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
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

/**
 *
 * @author Eric Sternberg (esternberg)
 */
public class FastJsonGenerator {

	private final static JsonFactory jsonFactory = new JsonFactory();
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
		jGen = jsonFactory.createJsonGenerator(writer).useDefaultPrettyPrinter();
		this.stateControl = new StateController(serializationManager, resource);
		try {
			this.serializeResource(resource);
		} catch (Throwable e) {
			LoggerFactory.getLogger(getClass()).error("error in serialization", e);
		}
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
		jGen = jsonFactory.createJsonGenerator(writer).useDefaultPrettyPrinter().setCodec(mapper);
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
			jGen.writeStringField("@type", this.figgureOutResourceType(resource));
			this.writeResourceBody(resource);
		}
		else {
			jGen.writeObjectFieldStart(this.figgureOutResourceType(resource));
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
	@SuppressWarnings("deprecation")
	private void writeSimpleResourceValue(Resource resource) throws IOException {
		if (!(resource instanceof SingleValueResource)) {
			return;
		}
		if (resource instanceof BooleanResource) {
			jGen.writeBooleanField("value", ((BooleanResource) resource).getValue());
		}
		else if (resource instanceof FloatResource) {
			jGen.writeNumberField("value", ((FloatResource) resource).getValue());
			if (resource instanceof PhysicalUnitResource) {
				PhysicalUnit u = ((PhysicalUnitResource) resource).getUnit();
				if (u != null) {
					jGen.writeStringField("unit", u.toString());
				}
			}
		}
		else if (resource instanceof IntegerResource) {
			jGen.writeNumberField("value", ((IntegerResource) resource).getValue());
		}
		else if (resource instanceof TimeResource) {
			jGen.writeNumberField("value", ((TimeResource) resource).getValue());
		}
		else if (resource instanceof StringResource) {
			jGen.writeStringField("value", ((StringResource) resource).getValue());
		}
		else if (resource instanceof org.ogema.core.model.simple.OpaqueResource) {
			// Jackson default Base64 variant (which is Base64Variants.MIME_NO_LINEFEEDS)
			jGen.writeBinaryField("value", ((org.ogema.core.model.simple.OpaqueResource) resource).getValue());
		}
	}

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
				jGen.writeBooleanField("value", v.getValue().getBooleanValue());
			}
		},
		FloatWriter(FloatValue.class) {
			@Override
			public void writeValue(SampledValue v, JsonGenerator jGen) throws IOException {
				jGen.writeNumberField("value", v.getValue().getFloatValue());
			}
		},
		IntWriter(IntegerValue.class) {
			@Override
			public void writeValue(SampledValue v, JsonGenerator jGen) throws IOException {
				jGen.writeNumberField("value", v.getValue().getIntegerValue());
			}
		},
		LongWriter(LongValue.class) {
			@Override
			public void writeValue(SampledValue v, JsonGenerator jGen) throws IOException {
				jGen.writeNumberField("value", v.getValue().getLongValue());
			}
		},
		StringWriter(StringValue.class) {
			@Override
			public void writeValue(SampledValue v, JsonGenerator jGen) throws IOException {
				jGen.writeStringField("value", v.getValue().getStringValue());
			}
		},
		DefaultWriter(Value.class) {
			@Override
			public void writeValue(SampledValue v, JsonGenerator jGen) throws IOException {
				jGen.writeStringField("value", v.getValue().getStringValue());
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
	private String figgureOutResourceType(Resource resource) {

		if (resource instanceof SingleValueResource) {
			if (resource instanceof FloatResource) {
				return FloatResource.class.getSimpleName();
			}
			return resource.getResourceType().getSimpleName();
		}
		if (resource instanceof Schedule) {
			return this.figgureOutScheduleType((Schedule) resource);
		}
		// return "Resource";
		// TODO: clarify why different cases in old json-serialization?
		return stateControl.isRquestedRootResource(resource) ? "Resource" : "resource";
	}

	/**
	 * We have to figgure out which (simple)type of Schedule we have ( (but there are no simple typs of schedules in the
	 * ogema model) otherwise deserialization of a shedule wont work.
	 *
	 * @param schedule
	 * @return
	 */
	private String figgureOutScheduleType(Schedule schedule) {
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
		return "Shedule";
	}

}
