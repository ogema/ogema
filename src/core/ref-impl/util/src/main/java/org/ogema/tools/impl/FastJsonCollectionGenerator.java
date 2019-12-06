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
import java.util.Collection;

import org.ogema.core.model.Resource;
import org.ogema.core.tools.SerializationManager;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerator;

public class FastJsonCollectionGenerator {

//	private JsonGenerator jGen = null;
	
	public static final String serialize(Collection<Resource> resources, SerializationManager sman) throws IOException {
		final StringWriter writer = new StringWriter();
		serialize(writer, resources, sman);
		return writer.toString();
	}
	
	public static final void serialize(Writer writer, Collection<Resource> resources, SerializationManager sman) throws IOException {
		JsonGenerator jGen = StaticJsonGenerator.createGenerator(writer);
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
