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
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.core.tools.SerializationManager;
import org.ogema.exam.OsgiAppTestBase;
import org.ogema.exam.ResourceAssertions;
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
        String resname = resman.getUniqueResourceName("CreateCustomListTest");
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
