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

import static org.ogema.serialization.JaxbResource.NS_OGEMA_REST;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import org.ogema.core.channelmanager.measurements.SampledValue;
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
import org.ogema.core.resourcemanager.InvalidResourceTypeException;
import org.ogema.core.resourcemanager.NoSuchResourceException;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceAlreadyExistsException;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.core.tools.SerializationManager;
import org.ogema.serialization.JaxbFactory;
import org.ogema.serialization.JaxbResource;
import org.ogema.serialization.JaxbResourceCollection;
import org.ogema.serialization.jaxb.ResourceCollection;
import org.ogema.serialization.jaxb.ResourceLink;
import org.ogema.serialization.jaxb.ScheduleResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.util.Objects;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Conversion tool between OGEMA objects and text strings. This is a singleton
 * construction designed to be used by SerializationManagerImpl. From the point
 * of this object, the SerializationManagers passed to the method calls are
 * merely the configuration for the serialization. Actual serialization methods
 * defined on the SerializationManager should not be called from this object.
 *
 * @author Timo Fischer, Fraunhofer IWES
 * @author Jan Lapp, Fraunhofer IWES
 */
final class SerializationCore {

	final ObjectMapper mapper;

	// experimental json serializer; note: non-fast serializer fails on
	// schedules (even worse than the fast one)
    @SuppressWarnings("deprecation")
	private final FastJsonGenerator fastJsonGenerator;
	private final boolean useFastJsonGenerator = true;

	private final Marshaller marshaller;
	private final Marshaller collectionsMarshaller;

	final ResourceAccess resacc;
	final ResourceManagement resman;
	final static Logger LOGGER = LoggerFactory.getLogger(SerializationCore.class);
	// JAXBContext is thread safe and expensive to build, initialize only once.
	private final static JAXBContext MARSHALLING_CONTEXT = AccessController.doPrivileged(new PrivilegedAction<JAXBContext>() {

		@Override
		public JAXBContext run() {
			return createMarshallingContext();
		}
	});
	private final static JAXBContext UNMARSHALLING_CONTEXT = AccessController.doPrivileged(new PrivilegedAction<JAXBContext>() {

		@Override
		public JAXBContext run() {
			return createUnmarshallingContext();
		}
	});
	private final static JAXBContext COLLECTIONS_MARSHALLING_CONTEXT = AccessController.doPrivileged(new PrivilegedAction<JAXBContext>() {

		@Override
		public JAXBContext run() {
			return createCollectionsMarshallingContext();
		}
	});
    private final static XMLInputFactory INPUT_FACTORY =  AccessController.doPrivileged(new PrivilegedAction<XMLInputFactory>() {

		@Override
		public XMLInputFactory run() {
			return XMLInputFactory.newFactory();
		}
	});
    
    static {
        /* XXE prevention: disable external entities, but keep DTD support for
        normal entities => Documents with external entities will load, but the
        external entity will be empty */
        INPUT_FACTORY.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        INPUT_FACTORY.setProperty(XMLInputFactory.SUPPORT_DTD, true);
    }

    @SuppressWarnings("deprecation")
	private SerializationCore(ResourceAccess resacc, ResourceManagement resman, Logger logger) {
		mapper = createJacksonMapper(true);
		this.resacc = resacc;
		this.resman = resman;
		try {
			marshaller = MARSHALLING_CONTEXT.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			fastJsonGenerator = new FastJsonGenerator();
			collectionsMarshaller = COLLECTIONS_MARSHALLING_CONTEXT.createMarshaller();
		} catch (JAXBException ex) {
			logger.error("Failed to create SerializationCore", ex);
			throw new RuntimeException(ex);
		}
	}

	protected SerializationCore(ResourceAccess resacc, ResourceManagement resman) {
		this(resacc, resman, org.slf4j.LoggerFactory.getLogger(SerializationCore.class));
	}
	
	private static JAXBContext createCollectionsMarshallingContext() {
		try {
			return JAXBContext.newInstance(JaxbResourceCollection.class.getPackage().getName(), JaxbResourceCollection.class.getClassLoader());
		} catch (JAXBException ex) {
			LOGGER.error("could not create JAXB marshalling context", ex);
			throw new RuntimeException(ex);
		}
	}

	private static JAXBContext createMarshallingContext() {
		try {
			return JAXBContext.newInstance("org.ogema.serialization", JaxbResource.class.getClassLoader());
		} catch (JAXBException ex) {
			LOGGER.error("could not create JAXB marshalling context", ex);
			throw new RuntimeException(ex);
		}
	}

	private static JAXBContext createUnmarshallingContext() {
		try {
			return JAXBContext.newInstance("org.ogema.serialization.jaxb",
					org.ogema.serialization.jaxb.Resource.class.getClassLoader());
		} catch (JAXBException ex) {
			LOGGER.error("could not create JAXB unmarshalling context", ex);
			throw new RuntimeException(ex);
		}
	}

	protected static ObjectMapper createJacksonMapper() {
		return createJacksonMapper(true);
	}

	protected static ObjectMapper createJacksonMapper(boolean indent) {
        @SuppressWarnings("deprecation")
		AnnotationIntrospector spec = AnnotationIntrospector.pair(new JacksonAnnotationIntrospector(),
				new com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector());
		ObjectMapper mapper = new ObjectMapper();
        @SuppressWarnings("deprecation")
		SimpleModule testModule = new SimpleModule("MyModule", new com.fasterxml.jackson.core.Version(1, 0, 0, null));
		testModule.addSerializer(new ResourceSerializer(mapper));
		mapper.registerModule(testModule);
		mapper.setAnnotationIntrospector(spec);
		mapper.configure(SerializationFeature.INDENT_OUTPUT, indent);
		return mapper;
	}

	String toJson(Resource resource, SerializationManager manager) {
		try {
			// use new json serializier
			if (useFastJsonGenerator) {
				return fastJsonGenerator.serialize(resource, manager);
			} else {
				final JaxbResource jres = JaxbFactory.createJaxbResource(resource, manager);
				return mapper.writeValueAsString(jres);
			}

		} catch (IOException ioex) {
			LOGGER.error("JSON serialization failed for resource {}", resource, ioex);
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
			LOGGER.error("JSON serialization failed for object {}", object, ioex);
			return null;
		}
	}

	String toXml(Resource resource, SerializationManager manager) {
		StringWriter sw = new StringWriter(200);
		try {
			final JaxbResource jres = JaxbFactory.createJaxbResource(resource, manager);
			// wrapping is required for 'xsi:type' attribute.
			JAXBElement<?> e = new JAXBElement<>(new QName(JaxbResource.NS_OGEMA_REST, "resource", "og"),
					JaxbResource.class, jres);
			marshaller.marshal(e, sw);
		} catch (JAXBException jaxb) {
			// XXX
			LOGGER.warn("XML serialization failed for resource {}", resource, jaxb);
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
		} else {
			mapper.writeValue(output, JaxbFactory.createJaxbResource(resource, manager));
		}
	}

	// void writeJson(Writer output, String name, JSWidget widget) {
	// throw new UnsupportedOperationException("Not supported yet.");
	// }
	void writeJson(Writer output, ResourcePattern<?> rad) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	void writeJson(Writer output, Object object, SerializationManager manager) throws IOException {
		if (useFastJsonGenerator) {
			fastJsonGenerator.serialize(output, object, manager);
		} else {
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
			LOGGER.error("JSON serialization failed for resource {}", sched, ioex);
			return null;
		}
	}

	String toXml(Schedule sched, long start, long end, SerializationManager sman) {
		StringWriter output = new StringWriter();
		try {
			writeXml(output, sched, start, end, sman);
			return output.toString();
		} catch (IOException ioex) {
			LOGGER.error("XML serialization failed for resource {}", sched, ioex);
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

	// void writeXml(Writer output, String name, JSWidget widget) {
	// throw new UnsupportedOperationException("Not supported yet.");
	// }
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
			LOGGER.error("import of JSON resource into OGEMA failed; target resource {}", resource, ex);
		}
	}

	void applyXml(String xml, Resource resource, boolean forceUpdate) {
		applyXml(new StringReader(xml), resource, forceUpdate);
	}

	void applyXml(Reader xmlReader, Resource resource, boolean forceUpdate) {
		try {
			apply(deserializeXml(xmlReader), resource, forceUpdate);
		} catch (IOException | ClassNotFoundException ex) {
			LOGGER.error("import of XML resource into OGEMA failed; target resource {}", resource, ex);
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

	protected org.ogema.serialization.jaxb.Resource deserializeJson(Reader reader) throws IOException, ClassNotFoundException {
        return new JsonReaderJackson().read(reader);
		//return mapper.readValue(reader, org.ogema.serialization.jaxb.Resource.class);
	}

	protected org.ogema.serialization.jaxb.Resource deserializeXml(Reader reader) throws IOException {
        final XMLStreamReader src;
        try {
            src = INPUT_FACTORY.createXMLStreamReader(reader);
        } catch (XMLStreamException ex) {
            throw new IOException(ex);
        }
		try {
			return (org.ogema.serialization.jaxb.Resource) AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {

				@SuppressWarnings("rawtypes")
				@Override
				public Object run() throws JAXBException {
                    Object o = UNMARSHALLING_CONTEXT.createUnmarshaller().unmarshal(src);
                    return (o instanceof JAXBElement)? ((JAXBElement)o).getValue() : o;
				}

				
			});
		} catch (PrivilegedActionException e) {
			throw new IOException(e.getCause());
		}
	}
    
    protected <T extends Resource> T create(org.ogema.serialization.jaxb.Resource input, Resource target) {
        Set<LinkInfo> unresolvedLinks = new HashSet<>();
        T rval = createInternal(input, target, unresolvedLinks);
        for (LinkInfo link: unresolvedLinks) {
            // create missing links between different top level resources
            //XXX works outside of activation transactions.
            Resource linkParent = resacc.getResource(link.parent);
            if (linkParent == null || !linkParent.exists()) {
                LOGGER.warn("invalid link: link parent '{}' does not exist", link.parent);
                continue;
            }
            Resource linkTarget = resacc.getResource(link.target);
            if (linkTarget == null || !linkTarget.exists()) {
                LOGGER.warn("invalid link: link target '{}' does not exist", link.target);
                continue;
            }
            Class<? extends Resource> linkType;
            try {
                linkType = Class.forName(link.type).asSubclass(Resource.class);
                linkParent.getSubResource(link.name, linkType).setAsReference(linkTarget);
            } catch (ClassNotFoundException ex) {
                LOGGER.warn("invalid link: unknown type: {}", link.type);
            } catch (ResourceAlreadyExistsException e) {
            	LOGGER.warn("invalid link: resource exists with invalid type.", e);
            }
        }
        return rval;
    }

	/**
	 * create the input resource as new subresource of target, or as new top
	 * level resource if target is null.
	 */
	protected <T extends Resource> T createInternal(org.ogema.serialization.jaxb.Resource input, Resource target, Set<LinkInfo> unresolvedLinks) {
		try {
			@SuppressWarnings("unchecked")
			final Class<? extends Resource> inputOgemaType = (Class<? extends Resource>) Class.forName(input.getType());
			if (!Resource.class.isAssignableFrom(inputOgemaType)) {
				throw new InvalidResourceTypeException("illegal type in input data structure: " + inputOgemaType);
			}
			String name = input.getName();
			if (name.isEmpty() && "/".equals(input.getPath())) {
				if (target != null) {
					throw new NoSuchResourceException("empty name");
				}
				// add to root
				for (Object o : input.getSubresources()) {
					if (o instanceof org.ogema.serialization.jaxb.Resource) {
						createInternal((org.ogema.serialization.jaxb.Resource) o, null, unresolvedLinks);
					} else {
						LOGGER.error("trying to create top level link: {}", o.getClass());
					}
				}
				@SuppressWarnings("unchecked")
				T rval = (T) target;
				return rval;
			} else {
				if (target == null) {
					target = resacc.getResource(name);
					if (target != null) {
						// XXX exception?
						// throw new ResourceAlreadyExistsException("resource
						// already exists: " + name);
					} else {
						target = resman.createResource(name, inputOgemaType.asSubclass(Resource.class));
					}
				} else {
					Resource sub = target.getSubResource(name, inputOgemaType);
					if (!sub.exists()) {
						target.addDecorator(name, inputOgemaType);
					}
					target = sub;
				}
				unresolvedLinks.addAll(apply(input, target, true));
			}
			@SuppressWarnings("unchecked")
			T rval = resacc.getResource(target.getPath());// (T) target;
			return rval;
		} catch (ClassNotFoundException cnfe) {
			throw new InvalidResourceTypeException("class not found", cnfe);
		}
	}

	/**
	 * @param input
	 *            a representation of an OGEMA resource which has been
	 *            deserialized from JSON or XML.
	 * @param target
	 *            the OGEMA resource to be updated with information from
	 *            {@code input}
	 * @param forceUpdate
	 *            apply updates even if data in the OGEMA model already matches
	 *            the input data.
	 * @throws ClassNotFoundException
	 *             if a resource type string used in the input does not
	 *             represent an available class.
	 * @throws IllegalArgumentException
	 *             if a resource type string used in the input does not
	 *             represent an OGEMA resource type.
	 */
	// TODO exception handling
	@SuppressWarnings("unchecked")
	protected Set<LinkInfo> apply(org.ogema.serialization.jaxb.Resource input, Resource target, boolean forceUpdate)
			throws ClassNotFoundException {
		Set<LinkInfo> unresolvedLinks = Collections.emptySet();
		Set<LinkInfo> lastUnresolvedLinks;
        @SuppressWarnings("deprecation")
		org.ogema.core.resourcemanager.Transaction trans = resacc.createTransaction();
		Collection<Resource> resourcesToActivate = new HashSet<>();
		Collection<Resource> resourcesToDeactivate = new HashSet<>();
		do {
			lastUnresolvedLinks = unresolvedLinks;
			unresolvedLinks = applyInternal(input, target, forceUpdate, trans, resourcesToActivate,
					resourcesToDeactivate);
			// repeat until there are no more unresolved links, or the set
			// of unresolved links doesn't change any more (-> input broken or
			// refering to deleted resources)
		} while (!(unresolvedLinks.isEmpty() || unresolvedLinks.equals(lastUnresolvedLinks)));
        @SuppressWarnings("deprecation")
		org.ogema.core.resourcemanager.Transaction deactivate = resacc.createTransaction();
		deactivate.addResources(resourcesToDeactivate);
		// FIXME deactivation, setting of values and activation are not
		// synchronized
		deactivate.deactivate();
		trans.write();
        @SuppressWarnings("deprecation")
		org.ogema.core.resourcemanager.Transaction activate = resacc.createTransaction();
		activate.addResources(resourcesToActivate);
		activate.activate();
        
        return unresolvedLinks;
	}
    
    static class LinkInfo {
        
        final String parent;
        final String name;
        final String target;
        final String type;

        public LinkInfo(String parent, String name, String target, String type) {
            this.name = name;
            this.parent = parent;
            this.target = target;
            this.type = type;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 47 * hash + Objects.hashCode(this.parent);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final LinkInfo other = (LinkInfo) obj;
            if (!Objects.equals(this.parent, other.parent)) {
                return false;
            }
            if (!Objects.equals(this.name, other.name)) {
                return false;
            }
            if (!Objects.equals(this.target, other.target)) {
                return false;
            }
            if (!Objects.equals(this.type, other.type)) {
                return false;
            }
            return true;
        }
        
        
        
    }

	// returns the set of unresolved (not existing) links contained in the
	// serialized input
	@SuppressWarnings("unchecked")
	private Set<LinkInfo> applyInternal(org.ogema.serialization.jaxb.Resource input, Resource target, boolean forceUpdate,
			@SuppressWarnings("deprecation") org.ogema.core.resourcemanager.Transaction trans, Collection<Resource> resourcesToActivate, Collection<Resource> resourcesToDeactivate)
			throws ClassNotFoundException {
		Set<LinkInfo> unresolvedLinks = new HashSet<>();
		final Class<?> inputOgemaType = Class.forName(input.getType());
		if (!Resource.class.isAssignableFrom(inputOgemaType)) {
			throw new IllegalArgumentException("illegal type in input data structure: " + inputOgemaType);
		}
		if (input instanceof org.ogema.serialization.jaxb.ResourceList) {
			org.ogema.serialization.jaxb.ResourceList inputList = (org.ogema.serialization.jaxb.ResourceList) input;
			if (target instanceof ResourceList && (inputList.getElementType() != null)) {
				ResourceList<?> targetList = (ResourceList) target;
				if (targetList.getElementType() == null) {
					Class<? extends Resource> elementType = (Class<? extends Resource>) Class
							.forName(inputList.getElementType());
					targetList.setElementType(elementType);
				}
			} else {
				// XXX should probably raise an exception
			}
		}
		if (SingleValueResource.class.isAssignableFrom(target.getResourceType())) {
			saveSimpleTypeData(input, target, forceUpdate, trans);
		}

		if (ArrayResource.class.isAssignableFrom(target.getResourceType())) {
			saveArrayTypeData(input, target, forceUpdate, trans);
		}

		if (Schedule.class.isAssignableFrom(target.getResourceType())) {
			saveScheduleData(input, target, trans);
		}

		if (input.isActive() != null) {
			if (target.isActive() ^ input.isActive()) {
				if (target.isActive()) {
					resourcesToDeactivate.add(target);
				} else {
					resourcesToActivate.add(target);
				}
			}
		}

		for (Object o : input.getSubresources()) {
			if (o instanceof org.ogema.serialization.jaxb.Resource) {
				org.ogema.serialization.jaxb.Resource subRes = (org.ogema.serialization.jaxb.Resource) o;
				Class<? extends Resource> subResType = (Class<? extends Resource>) Class.forName(subRes.getType());
				String name = subRes.getName();
				Resource ogemaSubRes = target.getSubResource(name, subResType);
				if (ogemaSubRes == null || !ogemaSubRes.exists()) {
                    // use addDecorator which allows sub resource to be of a subtype of the defined type
                    ogemaSubRes = target.addDecorator(name, subResType);
				}
				unresolvedLinks.addAll(applyInternal(subRes, ogemaSubRes, forceUpdate, trans, resourcesToActivate,
						resourcesToDeactivate));
			} else if (o instanceof ResourceLink) {
				ResourceLink link = (ResourceLink) o;
				Resource linkedResource = resacc.getResource(link.getLink());
				if (linkedResource == null || !linkedResource.exists()) {
					unresolvedLinks.add(new LinkInfo(target.getPath(), link.getName(), link.getLink(), link.getType()));
					continue;
				}
				Resource ogemaSubRes = target.getSubResource(link.getName());
				if (ogemaSubRes != null && ogemaSubRes.equalsLocation(linkedResource)) {
					continue;
				}
				if (isOptionalElement(link.getName(), target.getResourceType())) {
					target.setOptionalElement(link.getName(), linkedResource);
				} else {
					target.addDecorator(link.getName(), linkedResource);
				}
			} else {
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
	protected static void saveSimpleTypeData(org.ogema.serialization.jaxb.Resource input, Resource target, boolean forceUpdate,
			org.ogema.core.resourcemanager.Transaction trans) throws ClassNotFoundException {
		Class<?> inputType = Class.forName(input.getType());
		if (!target.getResourceType().isAssignableFrom(inputType)
				&& !(FloatResource.class.isAssignableFrom(target.getResourceType())
						&& FloatResource.class.isAssignableFrom(inputType))) {
			throw new IllegalArgumentException("incompatible types: " + inputType + " != " + target.getResourceType());
		}
		if (input instanceof org.ogema.serialization.jaxb.BooleanResource) {
			if (!forceUpdate && (((BooleanResource) target)
					.getValue() == ((org.ogema.serialization.jaxb.BooleanResource) input).isValue())) {
				return;
			}
			trans.addResource(target);// XXX
			trans.setBoolean((BooleanResource) target,
					((org.ogema.serialization.jaxb.BooleanResource) input).isValue());
			// ((BooleanResource)
			// target).setValue(((org.ogema.serialization.jaxb.BooleanResource)
			// input).isValue());
		} else if (input instanceof org.ogema.serialization.jaxb.FloatResource) {
			if (!forceUpdate && (((FloatResource) target)
					.getValue() == ((org.ogema.serialization.jaxb.FloatResource) input).getValue())) {
				return;
			}
			trans.addResource(target);// XXX
			trans.setFloat((FloatResource) target, ((org.ogema.serialization.jaxb.FloatResource) input).getValue());
			// ((FloatResource)
			// target).setValue(((org.ogema.serialization.jaxb.FloatResource)
			// input).getValue());
		} else if (input instanceof org.ogema.serialization.jaxb.IntegerResource) {
			if (!forceUpdate && (((IntegerResource) target)
					.getValue() == ((org.ogema.serialization.jaxb.IntegerResource) input).getValue())) {
				return;
			}
			trans.addResource(target);// XXX
			trans.setInteger((IntegerResource) target,
					((org.ogema.serialization.jaxb.IntegerResource) input).getValue());
			// ((IntegerResource)
			// target).setValue(((org.ogema.serialization.jaxb.IntegerResource)
			// input).getValue());
		} else if (input instanceof org.ogema.serialization.jaxb.OpaqueResource) {
			if (!forceUpdate && Arrays.equals(((org.ogema.core.model.simple.OpaqueResource) target).getValue(),
					((org.ogema.serialization.jaxb.OpaqueResource) input).getValue())) {
				return;
			}
			trans.addResource(target);// XXX
			trans.setByteArray((org.ogema.core.model.simple.OpaqueResource) target,
					((org.ogema.serialization.jaxb.OpaqueResource) input).getValue());
			// ((org.ogema.core.model.simple.OpaqueResource)
			// target).setValue(((org.ogema.serialization.jaxb.OpaqueResource)
			// input).getValue());
		} else if (input instanceof org.ogema.serialization.jaxb.StringResource) {
			if (!forceUpdate) {
				String s1 = ((StringResource) target).getValue();
				String s2 = ((org.ogema.serialization.jaxb.StringResource) input).getValue();
				if (s1 != null && s2 != null && s1.equals(s2)) {
					return;
				}
			}
			trans.addResource(target);// XXX
			trans.setString((StringResource) target, ((org.ogema.serialization.jaxb.StringResource) input).getValue());
			// ((StringResource)
			// target).setValue(((org.ogema.serialization.jaxb.StringResource)
			// input).getValue());
		} else if (input instanceof org.ogema.serialization.jaxb.TimeResource) {
			if (!forceUpdate && (((TimeResource) target)
					.getValue() == ((org.ogema.serialization.jaxb.TimeResource) input).getValue())) {
				return;
			}
			trans.addResource(target);// XXX
			trans.setTime((TimeResource) target, ((org.ogema.serialization.jaxb.TimeResource) input).getValue());
			// ((TimeResource)
			// target).setValue(((org.ogema.serialization.jaxb.TimeResource)
			// input).getValue());
		}
	}

	private static void saveScheduleData(org.ogema.serialization.jaxb.Resource input, Resource target, @SuppressWarnings("deprecation") org.ogema.core.resourcemanager.Transaction trans) {
		ScheduleResource jaxbSchedule = (ScheduleResource) input;
		Schedule ogemaSchedule = (Schedule) target;

		if (jaxbSchedule.getInterpolationMode() != null) {
			ogemaSchedule.setInterpolationMode(InterpolationMode.valueOf(jaxbSchedule.getInterpolationMode()));
		}
		// FIXME: set lastCalculationTime and lastUpdateTime (?)

		List<org.ogema.serialization.jaxb.SampledValue> jaxbValues = jaxbSchedule.getEntry();
		List<SampledValue> ogemaValues = new ArrayList<>(jaxbValues.size());

		for (org.ogema.serialization.jaxb.SampledValue v : jaxbValues) {
			ogemaValues.add(new SampledValue(v.createOgemaValue(), v.getTime(), v.getQuality()));
		}

		// XXX unsure about null values and XSD nillable and/or optional
		// elements, requires investigation (+ separate
		// unit tests)
		if (jaxbSchedule.getStart() != null) {
			long end = jaxbSchedule.getEnd() == null || jaxbSchedule.getEnd() == -1 ? Long.MAX_VALUE
					: jaxbSchedule.getEnd();
			ogemaSchedule.replaceValues(jaxbSchedule.getStart(), end, ogemaValues);
		} else {
			ogemaSchedule.addValues(ogemaValues);
		}
	}

    @SuppressWarnings("deprecation")
	private static void saveArrayTypeData(org.ogema.serialization.jaxb.Resource input, Resource target, boolean forceUpdate,
			@SuppressWarnings("deprecation") org.ogema.core.resourcemanager.Transaction trans) throws ClassNotFoundException {
		Class<?> inputType = Class.forName(input.getType());
		if (input instanceof org.ogema.serialization.jaxb.BooleanArrayResource) {
			trans.addResource(target); // XXX
			List<Boolean> values = ((org.ogema.serialization.jaxb.BooleanArrayResource) input).getValues();
			boolean[] arr = new boolean[values.size()];
			for (int i = 0; i < arr.length; i++) {
				arr[i] = values.get(i);
			}
			trans.setBooleanArray((BooleanArrayResource) target, arr);
		} else if (input instanceof org.ogema.serialization.jaxb.ByteArrayResource) {
			trans.addResource(target); // XXX
			byte[] values = ((org.ogema.serialization.jaxb.ByteArrayResource) input).getValues();
			trans.setByteArray((ByteArrayResource) target, values);
		} else if (input instanceof org.ogema.serialization.jaxb.FloatArrayResource) {
			trans.addResource(target); // XXX
			List<Float> values = ((org.ogema.serialization.jaxb.FloatArrayResource) input).getValues();
			float[] arr = new float[values.size()];
			for (int i = 0; i < arr.length; i++) {
				arr[i] = values.get(i);
			}
			trans.setFloatArray((FloatArrayResource) target, arr);
		} else if (input instanceof org.ogema.serialization.jaxb.IntegerArrayResource) {
			trans.addResource(target); // XXX
			List<Integer> values = ((org.ogema.serialization.jaxb.IntegerArrayResource) input).getValues();
			int[] arr = new int[values.size()];
			for (int i = 0; i < arr.length; i++) {
				arr[i] = values.get(i);
			}
			trans.setIntegerArray((IntegerArrayResource) target, arr);
		} else if (input instanceof org.ogema.serialization.jaxb.StringArrayResource) {
			// ((StringArrayResource)target).setValues(((org.ogema.serialization.jaxb.StringArrayResource)input).getValues().toArray(new
			// String[0]));
			trans.addResource(target); // XXX
			trans.setStringArray((StringArrayResource) target,
					((org.ogema.serialization.jaxb.StringArrayResource) input).getValues().toArray(new String[0]));
		} else if (input instanceof org.ogema.serialization.jaxb.TimeArrayResource) {
			trans.addResource(target); // XXX
			List<Long> values = ((org.ogema.serialization.jaxb.TimeArrayResource) input).getValues();
			long[] arr = new long[values.size()];
			for (int i = 0; i < arr.length; i++) {
				arr[i] = values.get(i);
			}
			trans.setTimeArray((TimeArrayResource) target, arr);
		} else if (input instanceof org.ogema.serialization.jaxb.OpaqueResource) {
			trans.addResource(target); // XXX
			byte[] values = ((org.ogema.serialization.jaxb.OpaqueResource) input).getValue();
            if (target instanceof org.ogema.core.model.simple.OpaqueResource) {
                trans.setByteArray((org.ogema.core.model.simple.OpaqueResource)target, values);
            } else {
                trans.setByteArray((ByteArrayResource)target, values);
            }
		}
	}

	/*
	 ***** Collections ******
	 */

	String toJson(Collection<Resource> resources, SerializationManager manager) {
		try {
			// use new json serializier
			if (useFastJsonGenerator) {
				return FastJsonCollectionGenerator.serialize(resources, manager);
			} else {
				final JaxbResourceCollection jres = JaxbFactory.createJaxbResources(resources, manager);
				return mapper.writeValueAsString(jres);
			}

		} catch (IOException ioex) {
			LOGGER.error("JSON serialization failed for resources {}", resources, ioex);
			return null;
		}
	}

	// FIXME context is expensive and should be recycled
	String toXml(Collection<Resource> resources, SerializationManager manager) {
		final StringWriter sw = new StringWriter(200);
        try {
            writeXml(sw, resources, manager);
        } catch (IOException ex) {
            LOGGER.warn("XML serialization failed for resources {}", resources, ex);
        }
        return sw.toString();
        /*
		try {
			final JaxbResourceCollection jres = JaxbFactory.createJaxbResources(resources, manager);
			// wrapping is required for 'xsi:type' attribute.
			final JAXBElement<?> e = new JAXBElement<>(
					new QName(NS_OGEMA_REST, "resources", "og"),
					JaxbResourceCollection.class, jres);
//			collectionsMarshaller.marshal(e, sw);
			AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {

				@Override
				public Void run() throws JAXBException {
					JAXBContext c = JAXBContext.newInstance(JaxbResourceCollection.class);
					c.createMarshaller().marshal(e, sw);
					return null;
				}
			});
		} catch (PrivilegedActionException jaxb) {
			// XXX
			LOGGER.warn("XML serialization failed for resources {}", resources, jaxb.getCause());
		}
		return sw.toString();
        */
	}

	@SuppressWarnings("unchecked")
	protected Collection<org.ogema.serialization.jaxb.Resource> deserializeJsonCollection(Reader reader) throws IOException {
        return new JsonReaderJackson().readCollection(reader);
	}

	protected static ResourceCollection deserializeXmlCollection(Reader reader) throws IOException {
		final XMLStreamReader src;
        try {
            src = INPUT_FACTORY.createXMLStreamReader(reader);
        } catch (XMLStreamException ex) {
            throw new IOException(ex);
        }
		try {
			return (ResourceCollection) AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {

				@SuppressWarnings("rawtypes")
				@Override
				public Object run() throws JAXBException {
                    //XXX moxy and the reference implementation return different things here
                    Object o = UNMARSHALLING_CONTEXT.createUnmarshaller().unmarshal(src);
                    return (o instanceof ResourceCollection)
                            ? o
                            : ((JAXBElement) o).getValue();
				}

				
			});
		} catch (PrivilegedActionException e) {
			throw new IOException(e.getCause());
		}
	}
	
	protected Collection<Resource> create(ResourceCollection input, Resource target) throws CloneNotSupportedException {
		List<Resource> resources = new ArrayList<>();
		for (Object res: input.getSubresources()) {
			try {
				Resource result = create((org.ogema.serialization.jaxb.Resource) res, target);
				if (result != null)
					resources.add(result);
			} catch (Exception e) {
				LOGGER.error("Error deserializing a collection of resources for target {}", target,e);
			}
		}
		return resources;
	}
	
	protected Collection<Resource> create(Collection<org.ogema.serialization.jaxb.Resource> input, Resource target) {
		List<Resource> resources = new ArrayList<>();
		for (org.ogema.serialization.jaxb.Resource res: input) {
			try {
				Resource result = create(res, target);
				if (result != null)
					resources.add(result);
			} catch (Exception e) {
				LOGGER.error("Error deserializing a collection of resources for target {}", target,e);
			}
		}
		return resources;
	}
	
	void writeJson(Writer output, Collection<Resource> resources, SerializationManager manager) throws IOException {
		if (useFastJsonGenerator) {
			FastJsonCollectionGenerator.serialize(output, resources, manager);
		} else {
			mapper.writeValue(output, JaxbFactory.createJaxbResources(resources, manager));
		}
	}
	
	void writeXml(Writer output, Collection<Resource> resources, SerializationManager manager) throws IOException {
		try {
			JaxbResourceCollection jaxb = JaxbFactory.createJaxbResources(resources, manager);
			// wrapping is required for 'xsi:type' attribute.
			JAXBElement<?> e = new JAXBElement<>(new QName(NS_OGEMA_REST, "resources", "og"),
					JaxbResourceCollection.class, jaxb);
			collectionsMarshaller.marshal(e, output);
			// marshaller.marshal(o, output);
		} catch (JAXBException ex) {
			throw new IOException(ex.getLocalizedMessage(), ex);
		}
	}

}
