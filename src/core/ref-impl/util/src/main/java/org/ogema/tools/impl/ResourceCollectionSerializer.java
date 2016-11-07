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
