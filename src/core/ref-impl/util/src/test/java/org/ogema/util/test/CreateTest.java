/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
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
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.core.tools.SerializationManager;
import org.ogema.exam.OsgiAppTestBase;
import org.ogema.model.actors.MultiSwitch;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

/**
 *
 * @author jlapp
 */
@ExamReactorStrategy(PerClass.class)
public class CreateTest extends OsgiAppTestBase {

	ResourceManagement resman;
	ResourceAccess resacc;
	int counter = 0;

	@Before
	public void setup() {
		resman = getApplicationManager().getResourceManagement();
		resacc = getApplicationManager().getResourceAccess();
	}

	@Test
	public void createAsSubResourceWorks() {
		MultiSwitch sw = resman.createResource("CreateTest_" + counter++, MultiSwitch.class);
		MultiSwitch sw2 = resman.createResource("CreateTest_" + counter++, MultiSwitch.class);
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

		MultiSwitch sw = resman.createResource("CreateTest_" + counter++, MultiSwitch.class);
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

}
