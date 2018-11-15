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
package org.ogema.util.test;

import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.ogema.core.model.ResourceList;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.core.tools.SerializationManager;
import org.ogema.exam.OsgiAppTestBase;
import org.ogema.model.actors.MultiSwitch;
import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.Constants;

/**
 *
 * @author jlapp
 */
@ExamReactorStrategy(PerClass.class)
public class CreateTest extends OsgiAppTestBase {

	ResourceManagement resman;
	ResourceAccess resacc;
	int counter = 0;
    
    @ProbeBuilder
	public TestProbeBuilder buildCustomProbe(TestProbeBuilder builder) {
		builder.setHeader(Constants.EXPORT_PACKAGE, "org.ogema.util.test");
		return builder;
	}

	@Before
	public void setup() {
		resman = getApplicationManager().getResourceManagement();
		resacc = getApplicationManager().getResourceAccess();
	}

	@Test
	public void createAsSubResourceWorks() {
		MultiSwitch sw = resman.createResource(newResourceName(), MultiSwitch.class);
		MultiSwitch sw2 = resman.createResource(newResourceName(), MultiSwitch.class);
		final float f = 47.11f;
		//		final String s = "hallo";

		sw.settings().controlLimits().upperLimit().create();
		sw.settings().controlLimits().upperLimit().setValue(f);
		//		sw.addOptionalElement("deviceInfo").addOptionalElement("deviceSoftware").addOptionalElement("description");
		//		sw.deviceInfo().deviceSoftware().addOptionalElement("controllerStatus");
		//		sw.deviceInfo().deviceSoftware().description().setValue(s);

		SerializationManager sman = getApplicationManager().getSerializationManager();
		sman.setMaxDepth(100);

		Assert.assertFalse(sw.settings().isActive());
		sw.settings().controlLimits().activate(false);
		//		sw.deviceInfo().deviceSoftware().activate(false);
		String xml = sman.toXml(sw.settings());

		Assert.assertFalse(sw2.settings().exists());
		sman.createFromXml(xml, sw2);

		assertNotNull(sw2.settings());
		assertNotNull(sw2.settings().controlLimits().upperLimit());
		assertEquals(f, sw2.settings().controlLimits().upperLimit().getValue(), 0.1f);
		assertFalse(sw2.settings().isActive());
		assertTrue(sw2.settings().controlLimits().isActive());
	}

	@Test
	public void createAsTopLevelWorks() {

		MultiSwitch sw = resman.createResource(newResourceName(), MultiSwitch.class);
		final float f = 47.11f;

		sw.settings().controlLimits().upperLimit().create();
		sw.settings().controlLimits().upperLimit().setValue(f);

		SerializationManager sman = getApplicationManager().getSerializationManager();
		sman.setMaxDepth(100);

		Assert.assertFalse(sw.settings().isActive());
		sw.settings().controlLimits().activate(false);
		String xml = sman.toXml(sw);

		String newName = sw.getName() + "_copy";
		assertNull(resacc.getResource(newName));
		xml = xml.replaceAll(sw.getName(), newName);
		sman.createFromXml(xml);

		MultiSwitch swNew = (MultiSwitch) resacc.getResource(newName);
		assertNotNull(swNew);
		assertFalse(sw.equalsLocation(swNew));
		assertNotNull(swNew.settings().controlLimits().upperLimit());
		assertEquals(f, swNew.settings().controlLimits().upperLimit().getValue(), 0.1f);
		assertFalse(swNew.settings().isActive());
		assertTrue(swNew.settings().controlLimits().isActive());
	}
    
    @Test public void createWorksForTopLevelCustomResourceLists() {
        String resname = newResourceName();
        @SuppressWarnings("unchecked")
        ResourceList<CustomType> l = resman.createResource(resname, ResourceList.class);
        l.setElementType(CustomType.class);
        CustomType e1 = l.add();
        e1.someData().create();
        e1.someData().setValue("foo");
        
        
        SerializationManager sman = getApplicationManager().getSerializationManager();
		sman.setMaxDepth(100);
		String xml = sman.toXml(l);
        
        String newname = resname + "_copy";
        xml = xml.replaceAll(resname, newname);
        //xml = xml.replace("<og:resource", "<og:resource xsi:type=\"og:ResourceList\"");
        //xml = xml.replace("<og:resource", "<og:resource xsi:type=\"fnord\"");
        System.out.println(xml);
        System.out.println("\n-------------------------------------------------\n");
        
        sman.createFromXml(xml);
        ResourceList<CustomType> lCopy = resacc.getResource(newname);
        assertNotNull(lCopy);
        
        System.out.println(sman.toXml(lCopy));
        assertEquals("element type must match", CustomType.class, lCopy.getElementType());
    }
    /*
    @Test public void createWorksForSingleValueResourceFields() {
        String resname = newResourceName();
        @SuppressWarnings("unchecked")
        CustomType ct = resman.createResource(resname, CustomType.class);
        //ct.unspecifiedValue().create();
        
        FloatResource value = resman.createResource(newResourceName(), FloatResource.class);
        ct.unspecifiedValue().setAsReference(value);
        
        value.setValue(47.11f);
        
        SerializationManager sman = getApplicationManager().getSerializationManager();
        //sman.setFollowReferences(false);
		sman.setMaxDepth(100);
		String xml = sman.toXml(ct);
        
        String newname = resname + "_copy";
        xml = xml.replaceAll(resname, newname);
        //xml = xml.replace("<og:resource", "<og:resource xsi:type=\"og:ResourceList\"");
        //xml = xml.replace("<og:resource", "<og:resource xsi:type=\"fnord\"");
        System.out.println(xml);
        System.out.println("\n-------------------------------------------------\n");
        
        sman.createFromXml(xml);
        CustomType copy = resacc.getResource(newname);
        assertNotNull(copy);
        
        ResourceAssertions.assertLocationsEqual(value, copy.unspecifiedValue());
        
        System.out.println(sman.toXml(copy));
    }
    */

}
