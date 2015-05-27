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
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig.Feature;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.map.module.SimpleModule;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.model.Resource;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.resourcemanager.InvalidResourceTypeException;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.core.tools.SerializationManager;
import org.ogema.serialization.JaxbFactory;
import org.ogema.serialization.JaxbResource;
import org.ogema.serialization.jaxb.ResourceLink;
import org.ogema.serialization.jaxb.ScheduleResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Conversion tool between OGEMA objects and text strings. This is a singleton construction designed to be used by
 * SerializationManagerImpl. From the point of this object, the SerializationManagers passed to the method calls are
 * merely the configuration for the serialization. Actual serialization methods defined on the SerializationManager
 * should not be called from this object.
 * 
 * @author Timo Fischer, Fraunhofer IWES
 * @author Jan Lapp, Fraunhofer IWES
 */
final class SerializationCore {

	final ObjectMapper mapper;

	private final FastJsonGenerator fastJsonGenerator; //experimental json serializer
	private final boolean useFastJsonGenerator = false;

	final Unmarshaller unmarshaller;
	final Marshaller marshaller;
	final ResourceAccess resacc;
	final ResourceManagement resman;
	final static Logger logger = LoggerFactory.getLogger(SerializationCore.class);
	// JAXBContext is thread safe and expensive to build, initialize only once.
	private final static JAXBContext marshallingContext = createMarshallingContext();
	private final static JAXBContext unmarshallingContext = createUnmarshallingContext();

	private SerializationCore(ResourceAccess resacc, ResourceManagement resman, Logger logger) {
		mapper = createJacksonMapper(true);
		this.resacc = resacc;
		this.resman = resman;
		try {
			unmarshaller = unmarshallingContext.createUnmarshaller();
			marshaller = marshallingContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			fastJsonGenerator = new FastJsonGenerator();
		} catch (JAXBException ex) {
			logger.error("Failed to create SerializationCore", ex);
			throw new RuntimeException(ex);
		}
	}

	protected SerializationCore(ResourceAccess resacc, ResourceManagement resman) {
		this(resacc, resman, org.slf4j.LoggerFactory.getLogger(SerializationCore.class));
	}

	private static JAXBContext createMarshallingContext() {
		try {
			return JAXBContext.newInstance("org.ogema.serialization", JaxbResource.class.getClassLoader());
		} catch (JAXBException ex) {
			logger.error("could not create JAXB marshalling context", ex);
			throw new RuntimeException(ex);
		}
	}

	private static JAXBContext createUnmarshallingContext() {
		try {
			return JAXBContext.newInstance("org.ogema.serialization.jaxb", org.ogema.serialization.jaxb.Resource.class
					.getClassLoader());
		} catch (JAXBException ex) {
			logger.error("could not create JAXB unmarshalling context", ex);
			throw new RuntimeException(ex);
		}
	}

	protected static ObjectMapper createJacksonMapper() {
		return createJacksonMapper(true);
	}

	protected static ObjectMapper createJacksonMapper(boolean indent) {
		AnnotationIntrospector spec = AnnotationIntrospector.pair(new JacksonAnnotationIntrospector(),
				new JaxbAnnotationIntrospector());
		ObjectMapper mapper = new ObjectMapper();
		SimpleModule testModule = new SimpleModule("MyModule", new Version(1, 0, 0, null));
		testModule.addSerializer(new ResourceSerializer(mapper)); // assuming serializer declares correct class to bind
		// to
		mapper.registerModule(testModule);
		mapper.setAnnotationIntrospector(spec);
		/*
		 * mapper.configure(SerializationConfig.Feature.WRAP_ROOT_VALUE, true);
		 * mapper.configure(DeserializationConfig.Feature.UNWRAP_ROOT_VALUE, false);
		 */
		mapper.configure(Feature.INDENT_OUTPUT, indent);
		return mapper;
	}

	String toJson(Resource resource, SerializationManager manager) {
		try {
			//use new json serializier
			if (useFastJsonGenerator) {
				return fastJsonGenerator.serialize(resource, manager);
			}
			else {
				final JaxbResource jres = JaxbFactory.createJaxbResource(resource, manager);
				return mapper.writeValueAsString(jres);
			}

		} catch (IOException ioex) {
			logger.error("JSON serialization failed", ioex);
			return null;
		}
	}

	String toJson(ResourcePattern<?> rad) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	String toJson(Object object, SerializationManager manager) {
		StringWriter output = new StringWriter();
		try {
			writeJson(output, object, manager);
			return output.toString();
		} catch (IOException ioex) {
			logger.error("JSON serialization failed", ioex);
			return null;
		}
	}

	String toXml(Resource resource, SerializationManager manager) {
		StringWriter sw = new StringWriter(200);
		try {
			final JaxbResource jres = JaxbFactory.createJaxbResource(resource, manager);
			marshaller.marshal(jres, sw);
		} catch (JAXBException jaxb) {
			// XXX
			logger.warn("XML serialization failed", jaxb);
		}
		return sw.toString();
	}

	String toXml(ResourcePattern<?> rad) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	String toXml(Object object) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	void writeJson(Writer output, Resource resource, SerializationManager manager) throws IOException {
		if (useFastJsonGenerator) {
			fastJsonGenerator.serialize(output, resource, manager);
		}
		else {
			mapper.writeValue(output, JaxbFactory.createJaxbResource(resource, manager));
		}
	}

	//	void writeJson(Writer output, String name, JSWidget widget) {
	//		throw new UnsupportedOperationException("Not supported yet.");
	//	}

	void writeJson(Writer output, ResourcePattern<?> rad) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	void writeJson(Writer output, Object object, SerializationManager manager) throws IOException {
		if (useFastJsonGenerator) {
			fastJsonGenerator.serialize(output, object, manager);
		}
		else {
			mapper.writeValue(output, object);
		}
	}

	void writeJson(Writer output, Schedule sched, long start, long end, SerializationManager sman) throws IOException {
		mapper.writeValue(output, JaxbFactory.createJaxbSchedule(sched, sman, start, end));
	}

	void writeXml(Writer output, Schedule sched, long start, long end, SerializationManager sman) throws IOException {
        try {
			JaxbResource jaxb = JaxbFactory.createJaxbSchedule(sched, sman, start, end);
			// wrapping is required for 'xsi:type' attribute.
			JAXBElement<?> e = new JAXBElement<>(new QName(JaxbResource.NS_OGEMA_REST, "resource", "og"),
					JaxbResource.class, jaxb);
			marshaller.marshal(e, output);
		} catch (JAXBException ex) {
			throw new IOException(ex.getLocalizedMessage(), ex);
		}
    }

	String toJson(Schedule sched, long start, long end, SerializationManager sman) {
		StringWriter output = new StringWriter();
		try {
			writeJson(output, sched, start, end, sman);
			return output.toString();
		} catch (IOException ioex) {
			logger.error("JSON serialization failed", ioex);
			return null;
		}
	}

	String toXml(Schedule sched, long start, long end, SerializationManager sman) {
		StringWriter output = new StringWriter();
		try {
			writeXml(output, sched, start, end, sman);
			return output.toString();
		} catch (IOException ioex) {
			logger.error("XML serialization failed", ioex);
			return null;
		}
	}

	void writeXml(Writer output, Resource resource, SerializationManager manager) throws IOException {
		try {
			JaxbResource jaxb = JaxbFactory.createJaxbResource(resource, manager);
			// wrapping is required for 'xsi:type' attribute.
			JAXBElement<?> e = new JAXBElement<>(new QName(JaxbResource.NS_OGEMA_REST, "resource", "og"),
					JaxbResource.class, jaxb);
			marshaller.marshal(e, output);
			// marshaller.marshal(o, output);
		} catch (JAXBException ex) {
			throw new IOException(ex.getLocalizedMessage(), ex);
		}
	}

	//	void writeXml(Writer output, String name, JSWidget widget) {
	//		throw new UnsupportedOperationException("Not supported yet.");
	//	}

	void writeXml(Writer output, ResourcePattern<?> rad) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	void writeXml(Writer output, Object object) throws IOException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	void applyJson(String json, Resource resource, boolean forceUpdate) {
		applyJson(new StringReader(json), resource, forceUpdate);
	}

	void applyJson(Reader jsonReader, Resource resource, boolean forceUpdate) {
		try {
			apply(deserializeJson(jsonReader), resource, forceUpdate);
		} catch (IOException | ClassNotFoundException ex) {
			logger.warn("import of JSON resource into OGEMA failed", ex);
		}
	}

	void applyXml(String xml, Resource resource, boolean forceUpdate) {
		applyXml(new StringReader(xml), resource, forceUpdate);
	}

	void applyXml(Reader xmlReader, Resource resource, boolean forceUpdate) {
		try {
			apply(deserializeXml(xmlReader), resource, forceUpdate);
		} catch (IOException | ClassNotFoundException ex) {
			logger.warn("import of XML resource into OGEMA failed", ex);
		}
	}

	void applyJson(String json, boolean forceUpdate) {
		applyJson(new StringReader(json), forceUpdate);
	}

	void applyJson(Reader jsonReader, boolean forceUpdate) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	void applyXml(String xml, boolean forceUpdate) {
		applyXml(new StringReader(xml), forceUpdate);
	}

	void applyXml(Reader xmlReader, boolean forceUpdate) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	protected org.ogema.serialization.jaxb.Resource deserializeJson(Reader reader) throws IOException {
		return mapper.readValue(reader, org.ogema.serialization.jaxb.Resource.class);
	}

	protected org.ogema.serialization.jaxb.Resource deserializeXml(Reader reader) throws IOException {
		Source src = new StreamSource(reader);
		try {
			return unmarshallingContext.createUnmarshaller()
					.unmarshal(src, org.ogema.serialization.jaxb.Resource.class).getValue();
		} catch (JAXBException ex) {
			throw new IOException(ex);
		}
	}

	/**
	 * create the input resource as new subresource of target, or as new top level resource
	 * if target is null.
	 */
	protected <T extends Resource> T create(org.ogema.serialization.jaxb.Resource input, Resource target) {
		try {
			@SuppressWarnings("unchecked")
			final Class<? extends Resource> inputOgemaType = (Class<? extends Resource>) Class.forName(input.getType());
			if (!Resource.class.isAssignableFrom(inputOgemaType)) {
				throw new InvalidResourceTypeException("illegal type in input data structure: " + inputOgemaType);
			}
			String name = input.getName();
			if (target == null) {
				target = resacc.getResource(name);
				if (target != null) {
					//XXX exception?
					//throw new ResourceAlreadyExistsException("resource already exists: " + name);
				}
				else {
					target = resman.createResource(name, inputOgemaType.asSubclass(Resource.class));
				}
			}
			else {
				Resource sub = target.getSubResource(name, inputOgemaType);
				if (!sub.exists()) {
					sub.create();
				}
				target = sub;
			}
			apply(input, target, true);
			@SuppressWarnings("unchecked")
			T rval = resacc.getResource(target.getPath());//(T) target;
			return rval;
		} catch (ClassNotFoundException cnfe) {
			throw new InvalidResourceTypeException("class not found", cnfe);
		}
	}

	/**
	 * @param input
	 *            a representation of an OGEMA resource which has been deserialized from JSON or XML.
	 * @param target
	 *            the OGEMA resource to be updated with information from {@code input}
	 * @param forceUpdate
	 *            apply updates even if data in the OGEMA model already matches the input data.
	 * @throws ClassNotFoundException
	 *             if a resource type string used in the input does not represent an available class.
	 * @throws IllegalArgumentException
	 *             if a resource type string used in the input does not represent an OGEMA resource type.
	 */
	// TODO exception handling
	@SuppressWarnings("unchecked")
	protected void apply(org.ogema.serialization.jaxb.Resource input, Resource target, boolean forceUpdate)
			throws ClassNotFoundException {
		Set<String> unresolvedLinks = Collections.emptySet();
		Set<String> lastUnresolvedLinks;
		do {
			lastUnresolvedLinks = unresolvedLinks;
			unresolvedLinks = applyInternal(input, target, forceUpdate);
			// repeat until there are no more unresolved links, or the set
			// of unresolved links doesn't change any more (-> input broken or refering to deleted resources)
		} while (!(unresolvedLinks.isEmpty() || (unresolvedLinks.equals(lastUnresolvedLinks))));
	}

	// returns the set of unresolved (not existing) links contained in the serialized input
	@SuppressWarnings("unchecked")
	private Set<String> applyInternal(org.ogema.serialization.jaxb.Resource input, Resource target, boolean forceUpdate)
			throws ClassNotFoundException {
        Set<String> unresolvedLinks = new HashSet<>();
		final Class<?> inputOgemaType = Class.forName(input.getType());
		if (!Resource.class.isAssignableFrom(inputOgemaType)) {
			throw new IllegalArgumentException("illegal type in input data structure: " + inputOgemaType);
		}
		if (SingleValueResource.class.isAssignableFrom(target.getResourceType())) {
			saveSimpleTypeData(input, target, forceUpdate);
		}

		if (Schedule.class.isAssignableFrom(target.getResourceType())) {
			saveScheduleData(input, target);
		}

		if (target.isActive() ^ input.isActive()) {
			if (target.isActive()) {
				target.deactivate(false);
			}
			else {
				target.activate(false);
			}
		}
        
		for (Object o : input.getSubresources()) {
			if (o instanceof org.ogema.serialization.jaxb.Resource) {
				org.ogema.serialization.jaxb.Resource subRes = (org.ogema.serialization.jaxb.Resource) o;
				Class<? extends Resource> subResType = (Class<? extends Resource>) Class.forName(subRes.getType());
				String name = subRes.getName();
				Resource ogemaSubRes = target.getSubResource(name);
				if (ogemaSubRes == null || !ogemaSubRes.exists()) {
					if (isOptionalElement(name, target.getResourceType())) {
						ogemaSubRes = target.addOptionalElement(name);
					}
					else {
						ogemaSubRes = target.addDecorator(name, subResType);
					}
				}
				unresolvedLinks.addAll(applyInternal(subRes, ogemaSubRes, forceUpdate));
			}
			else if (o instanceof ResourceLink) {
				ResourceLink link = (ResourceLink) o;
				Resource linkedResource = resacc.getResource(link.getLink());
                if (linkedResource == null || !linkedResource.exists()){
                    unresolvedLinks.add(link.getLink());
                    continue;
                }
                Resource ogemaSubRes = target.getSubResource(link.getName());
                if (ogemaSubRes != null && ogemaSubRes.equalsLocation(linkedResource)) {
                    continue;
                }
                if (isOptionalElement(link.getName(), target.getResourceType())) {
                    target.setOptionalElement(link.getName(), linkedResource);
                }
                else {
                    target.addDecorator(link.getName(), linkedResource);
                }
			}
			else {
				throw new IllegalArgumentException("Invalid subresource element: " + o);
			}
		}
        return unresolvedLinks;
	}

	static boolean isOptionalElement(String elementName, Class<? extends Resource> type) {
		try {
			Method m = type.getMethod(elementName, (Class<?>[]) null);
			return Resource.class.isAssignableFrom(m.getReturnType());
		} catch (NoSuchMethodException e) {
			return false;
		}
	}

	@SuppressWarnings("deprecation")
	protected void saveSimpleTypeData(org.ogema.serialization.jaxb.Resource input, Resource target, boolean forceUpdate)
			throws ClassNotFoundException {
		Class<?> inputType = Class.forName(input.getType());
		if (!target.getResourceType().isAssignableFrom(inputType)) {
			throw new IllegalArgumentException("incompatible types: " + inputType + " != " + target.getResourceType());
		}
		if (input instanceof org.ogema.serialization.jaxb.BooleanResource) {
			if (!forceUpdate
					&& (((BooleanResource) target).getValue() == ((org.ogema.serialization.jaxb.BooleanResource) input)
							.isValue())) {
				return;
			}
			((BooleanResource) target).setValue(((org.ogema.serialization.jaxb.BooleanResource) input).isValue());
		}
		else if (input instanceof org.ogema.serialization.jaxb.FloatResource) {
			if (!forceUpdate
					&& (((FloatResource) target).getValue() == ((org.ogema.serialization.jaxb.FloatResource) input)
							.getValue())) {
				return;
			}
			((FloatResource) target).setValue(((org.ogema.serialization.jaxb.FloatResource) input).getValue());
		}
		else if (input instanceof org.ogema.serialization.jaxb.IntegerResource) {
			if (!forceUpdate
					&& (((IntegerResource) target).getValue() == ((org.ogema.serialization.jaxb.IntegerResource) input)
							.getValue())) {
				return;
			}
			((IntegerResource) target).setValue(((org.ogema.serialization.jaxb.IntegerResource) input).getValue());
		}
		else if (input instanceof org.ogema.serialization.jaxb.OpaqueResource) {
			if (!forceUpdate
					&& Arrays.equals(((org.ogema.core.model.simple.OpaqueResource) target).getValue(),
							((org.ogema.serialization.jaxb.OpaqueResource) input).getValue())) {
				return;
			}
			((org.ogema.core.model.simple.OpaqueResource) target)
					.setValue(((org.ogema.serialization.jaxb.OpaqueResource) input).getValue());
		}
		else if (input instanceof org.ogema.serialization.jaxb.StringResource) {
			if (!forceUpdate) {
				String s1 = ((StringResource) target).getValue();
				String s2 = ((org.ogema.serialization.jaxb.StringResource) input).getValue();
				if (s1 != null && s2 != null && s1.equals(s2)) {
					return;
				}
			}
			((StringResource) target).setValue(((org.ogema.serialization.jaxb.StringResource) input).getValue());
		}
		else if (input instanceof org.ogema.serialization.jaxb.TimeResource) {
			if (!forceUpdate
					&& (((TimeResource) target).getValue() == ((org.ogema.serialization.jaxb.TimeResource) input)
							.getValue())) {
				return;
			}
			((TimeResource) target).setValue(((org.ogema.serialization.jaxb.TimeResource) input).getValue());
		}
	}

	private void saveScheduleData(org.ogema.serialization.jaxb.Resource input, Resource target) {
		ScheduleResource jaxbSchedule = (ScheduleResource) input;
		Schedule ogemaSchedule = (Schedule) target;

		List<org.ogema.serialization.jaxb.SampledValue> jaxbValues = jaxbSchedule.getEntry();
		List<SampledValue> ogemaValues = new ArrayList<>(jaxbValues.size());

		for (org.ogema.serialization.jaxb.SampledValue v : jaxbValues) {
			ogemaValues.add(new SampledValue(v.createOgemaValue(), v.getTime(), v.getQuality()));
		}

		// XXX unsure about null values and XSD nillable and/or optional elements, requires investigation (+ separate
		// unit tests)
		if (jaxbSchedule.getStart() != null) {
            long end = jaxbSchedule.getEnd() == null || jaxbSchedule.getEnd() == -1?
                    Long.MAX_VALUE : jaxbSchedule.getEnd();
			ogemaSchedule.replaceValues(jaxbSchedule.getStart(), end, ogemaValues);
		}
		else {
			ogemaSchedule.addValues(ogemaValues);
		}
	}
}
