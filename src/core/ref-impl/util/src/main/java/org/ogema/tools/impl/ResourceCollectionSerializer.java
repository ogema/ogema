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
import java.util.Collection;

import org.ogema.core.model.Resource;
import org.ogema.serialization.JaxbFactory;
import org.ogema.serialization.JaxbResourceCollection;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * 
 * @author jlapp
 */
public class ResourceCollectionSerializer extends StdSerializer<Collection<Resource>> {

	private static final long serialVersionUID = 1L;
	private final ObjectMapper mapper;

	public ResourceCollectionSerializer(ObjectMapper mapper) {
		super(Collection.class, false);
		this.mapper = mapper;
	}

	@Override
	public void serialize(Collection<Resource> value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		JaxbResourceCollection jr = JaxbFactory.createJaxbResources(value, null); // FIXME?
		provider.defaultSerializeValue(jr, gen);
	}


}
