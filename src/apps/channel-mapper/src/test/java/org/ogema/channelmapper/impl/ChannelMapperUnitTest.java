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
package org.ogema.channelmapper.impl;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;

import org.junit.Test;

public class ChannelMapperUnitTest {

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
