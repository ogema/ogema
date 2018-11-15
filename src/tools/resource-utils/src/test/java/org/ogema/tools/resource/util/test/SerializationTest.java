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
package org.ogema.tools.resource.util.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.junit.Before;
import org.junit.Test;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.exam.OsgiAppTestBase;
import org.ogema.exam.ResourceAssertions;
import org.ogema.model.locations.Room;
import org.ogema.model.prototypes.PhysicalElement;
import org.ogema.tools.resource.util.SerializationUtils;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.xml.sax.SAXException;

@ExamReactorStrategy(PerClass.class)
public class SerializationTest extends OsgiAppTestBase {

	private ResourceManagement rm;

	public SerializationTest() {
		super(true);
	}
	
	@Before
	public void setup() {
		rm = getApplicationManager().getResourceManagement();
	}

	@Test
	public void removeSubresourcesWorks_JSON() {
		PhysicalElement dummy = rm.createResource(newResourceName(), PhysicalElement.class);
		Room room = dummy.location().room().create();
		room.name().<StringResource> create().setValue("test");
		room.getSubResource("testSubRes", StringResource.class).<StringResource> create().setValue("test2");
		room.temperatureSensor().reading().<TemperatureResource> create().setCelsius(23.2F);
		String json = getApplicationManager().getSerializationManager().toJson(dummy);
		json = SerializationUtils.removeSubresources(json, StringResource.class, true);
		System.out.println("Applying json " + json);
		room.delete();
		getApplicationManager().getSerializationManager().applyJson(json, dummy, false);
		ResourceAssertions.assertExists(room);
		assertEquals("Unexpected number of StringResource subresources", 0, room.getSubResources(StringResource.class,true).size());
		assertEquals("Unexpected number of subresources", 2, room.getSubResources(true).size());
		dummy.delete();
	}
	
	@Test
	public void removeSubresourcesWorks_XML() throws SAXException, IOException, ParserConfigurationException, TransformerConfigurationException, TransformerException, TransformerFactoryConfigurationError {
		PhysicalElement dummy = rm.createResource(newResourceName(), PhysicalElement.class);
		Room room = dummy.location().room().create();
		room.name().<StringResource> create().setValue("test");
		room.getSubResource("testSubRes", StringResource.class).<StringResource> create().setValue("test2");
		room.temperatureSensor().reading().<TemperatureResource> create().setCelsius(23.2F);
		String xml = getApplicationManager().getSerializationManager().toXml(dummy);
		xml = SerializationUtils.removeSubresourcesXml(xml, StringResource.class, true);
		System.out.println("Applying xml " + xml);
		room.delete();
		getApplicationManager().getSerializationManager().applyXml(xml, dummy, false);
		ResourceAssertions.assertExists(room);
		assertEquals("Unexpected number of StringResource subresources", 0, room.getSubResources(StringResource.class,true).size());
		assertEquals("Unexpected number of subresources", 2, room.getSubResources(true).size());
		dummy.delete();
	}

}
