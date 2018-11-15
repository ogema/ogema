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
package org.ogema.channelmapper.impl;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;

import org.junit.Test;

public class ChannelMapperUnitTest  {

	@Test
	public void writeAndRereadConfigurationTest() {
		PersistentConfiguration configuration = new PersistentConfiguration("test.xml");

		MappingConfiguration storage = configuration.getMappingConfiguration();

		MappedResource resource = new MappedResource("test", "res1");

		storage.addMappedResource(resource);

		resource = new MappedResource("org.ogema.models.CHPModel", "chp1");

		ChannelDescription channelDescription = new ChannelDescription("coap-ascii", null, "192.168.1.23:3337", null,
				"/sensors/temp1:float", (long) 1000, 1.0, 0.0);

		resource.addMappedChannel(new MappedElement("path.to.resource", channelDescription));
		resource.addMappedChannel(new MappedElement("path.to.second.resource", "1.232"));

		storage.addMappedResource(resource);

		configuration.write();

		configuration = new PersistentConfiguration("test.xml");

		configuration.read();

		MappingConfiguration mapping = configuration.getMappingConfiguration();

		List<MappedResource> mappedResources = mapping.getMappedResources();

		assertEquals(2, mappedResources.size());

		MappedResource resource1 = mappedResources.get(0);
		MappedResource resource2 = mappedResources.get(1);

		assertEquals("res1", resource1.getResourceName());
		assertEquals("test", resource1.getResourceType());
		assertEquals("chp1", resource2.getResourceName());
		assertEquals("org.ogema.models.CHPModel", resource2.getResourceType());

		File file = new File("test.xml");

		file.delete();
	}

	@Test
	public void createMapperInstance() {
		new ChannelMapperImpl();

		// channelMapperApp.start(null);

	}

}
