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
package org.ogema.rest.patternmimic;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import org.ogema.tools.impl.ResourceSerializer;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

public class SerializationManager {
	
	private static final JAXBContext ctx; // creation is expensive, should be done only once, then stored.

	static {
		try {
			ctx = JAXBContext.newInstance(FakePattern.class.getPackage().getName(), FakePattern.class.getClassLoader());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	// these objects are not thread-safe, using separate instances of the serialization manager is required. Alternatively, create them only when needed.
	private final Marshaller marshaller;
	private final Unmarshaller unmarshaller;
	private final ObjectMapper mapper;
	
	public SerializationManager() {
		try {
			marshaller = ctx.createMarshaller();
			unmarshaller = ctx.createUnmarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE); // FIXME property prettyPrint
			mapper = new ObjectMapper();
			AnnotationIntrospector spec = AnnotationIntrospector.pair(new JacksonAnnotationIntrospector(),
					new JaxbAnnotationIntrospector(TypeFactory.defaultInstance())); // ok?
			SimpleModule testModule = new SimpleModule("RestModule", new Version(1, 0, 0, null, "org.ogema.ref-impl","rest")); // what is this good for?
			testModule.addSerializer(new ResourceSerializer(mapper));
			mapper.registerModule(testModule);
			mapper.setSerializationInclusion(Include.NON_NULL);
			mapper.setAnnotationIntrospector(spec);
			/*
			 * mapper.configure(SerializationConfig.Feature.WRAP_ROOT_VALUE, true);
			 * mapper.configure(DeserializationConfig.Feature.UNWRAP_ROOT_VALUE,
			 * false);
			 */
			mapper.configure(SerializationFeature.INDENT_OUTPUT, true); // FIXME indentation
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Could not create serialization manager");
		}
		
	}
	
	public String toXml(PatternMatch match) throws JAXBException {
		StringWriter writer = new StringWriter();
		JAXBElement<?> e = new JAXBElement<>(new QName(FakePattern.NS_OGEMA_REST_PATTERN, "match", "og"),
				PatternMatch.class, match);
		marshaller.marshal(e, writer);
		return writer.toString();
	}
	
	public String toJson(PatternMatch match) throws JsonGenerationException, JsonMappingException, IOException {
		StringWriter writer = new StringWriter();
		mapper.writeValue(writer, match);
		return writer.toString();
	}
	
	public String toXml(PatternMatchList matches) throws JAXBException {
		StringWriter writer = new StringWriter();
		JAXBElement<?> e = new JAXBElement<>(new QName(FakePattern.NS_OGEMA_REST_PATTERN, "patternMatchList", "og"),
				PatternMatchList.class, matches);
		marshaller.marshal(e, writer);
		return writer.toString();
	}
	
	public String toJson(PatternMatchList matches) throws JsonGenerationException, JsonMappingException, IOException {
		StringWriter writer = new StringWriter();
		mapper.writeValue(writer, matches);
		return writer.toString();
	}
	
	public FakePattern fromXml(String xml) throws JAXBException {
		StringReader reader = new StringReader(xml);
		@SuppressWarnings("unchecked")
		JAXBElement<FakePattern> el = (JAXBElement<FakePattern>) unmarshaller.unmarshal(reader);
		return el.getValue();
	}
	
	/**
	 * 
	 * @param json
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 * @return either a {@link FakePattern} or a {@link PatternMatch}, depending on the content of json
	 */
	// 
	public FakePattern fromJson(String json) throws JsonParseException, JsonMappingException, IOException {
		return mapper.readValue(json, FakePattern.class);
	}
	
	// mainly needed for testing
	public String toXml(FakePattern pattern) throws JAXBException { 
		StringWriter writer = new StringWriter();
		JAXBElement<?> e = new JAXBElement<>(new QName(FakePattern.NS_OGEMA_REST_PATTERN, "pattern", "og"),
				FakePattern.class, pattern);
		marshaller.marshal(e, writer);
		return writer.toString();
	}
	
	// mainly needed for testing
	public String toJson(FakePattern pattern) throws JsonGenerationException, JsonMappingException, IOException {
		StringWriter writer = new StringWriter();
		mapper.writeValue(writer, pattern);
		return writer.toString();
	}
	
}
