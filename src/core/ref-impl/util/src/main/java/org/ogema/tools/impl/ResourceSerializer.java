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
import org.ogema.core.model.Resource;
import org.ogema.core.timeseries.ReadOnlyTimeSeries;
import org.ogema.serialization.JaxbFactory;
import org.ogema.serialization.JaxbLink;
import org.ogema.serialization.JaxbResource;
import org.ogema.serialization.SerializationStatus;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * 
 * @author jlapp
 */
public class ResourceSerializer extends StdSerializer<Resource> {
    private static final long serialVersionUID = 1L;

	private final ObjectMapper mapper;

	public ResourceSerializer(ObjectMapper mapper) {
		super(Resource.class);
		this.mapper = mapper;
	}

	@Override
	public void serialize(Resource t, JsonGenerator jg, SerializerProvider sp) throws IOException,
			JsonGenerationException {
		if (t instanceof ReadOnlyTimeSeries) {
			sp.defaultSerializeValue(new JaxbLink(t), jg);
		}
		else {
			JaxbResource jr = JaxbFactory.createJaxbResource(t, (SerializationStatus) null);// new JaxbResource(t,  // FIXME?
			// true, true, true);
			sp.defaultSerializeValue(jr, jg);
		}
	}

}
