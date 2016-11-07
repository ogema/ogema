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
import java.util.Collection;

import org.ogema.core.model.Resource;
import org.ogema.core.tools.SerializationManager;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

public class FastJsonCollectionGenerator {

	private final static JsonFactory jsonFactory = StaticJsonGenerator.jsonFactory;
//	private JsonGenerator jGen = null;
	
	public static final String serialize(Collection<Resource> resources, SerializationManager sman) throws IOException {
		final StringWriter writer = new StringWriter();
		serialize(writer, resources, sman);
		return writer.toString();
	}
	
	public static final void serialize(Writer writer, Collection<Resource> resources, SerializationManager sman) throws IOException {
		JsonGenerator jGen = jsonFactory.createGenerator(writer).useDefaultPrettyPrinter();
//		jGen.writeStartObject();
		jGen.writeStartArray();
		for (Resource res: resources) {
			StateController stateControl = new StateController(sman,res); 
			try {
				StaticJsonGenerator.serializeResource(jGen,stateControl,res); // FIXME does this add an unwanted header?
			} catch (Throwable e) {
				LoggerFactory.getLogger(FastJsonCollectionGenerator.class).error("error in serialization", e);
			}
		}
		jGen.writeEndArray();
//		jGen.writeEndObject();
		jGen.flush();
	}
	
}
