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
package org.ogema.persistence.impl.mem;

import static org.junit.Assert.*;

import org.junit.Test;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.resourcemanager.ResourceNotFoundException;
import org.ogema.model.actors.OnOffSwitch;
import org.ogema.resourcetree.TreeElement;

/**
 * 
 * @author jlapp
 */
public class TreeElementTest extends TestBase {

	@Test
	public void getChildReturnsNullForNonExisting() throws Exception {
		db.addOrUpdateResourceType(OnOffSwitch.class);
		db.addResource("switch", OnOffSwitch.class, getAppId());
		TreeElement el = db.getToplevelResource("switch");
		TreeElement noSuchElement = el.getChild("fnord");
		assertNull(noSuchElement);
	}

	@Test
	public void addChildWorksForOptionalElements() throws Exception {
		db.addOrUpdateResourceType(OnOffSwitch.class);
		db.addResource("switch2", OnOffSwitch.class, getAppId());
		TreeElement el = db.getToplevelResource("switch2");
		int childCount = el.getChildren().size();
		TreeElement state = el.addChild("stateControl", BooleanResource.class, false);
		assertEquals(childCount + 1, el.getChildren().size());
		assertNotNull(state);
		assertEquals("stateControl", state.getName());
		// System.err.println(el.getChildren());
		for (TreeElement child : el.getChildren()) {
			if (child.getName().equals("stateControl") && child.getResID() == state.getResID()) {
				return;
			}
		}
		fail("child not found");
	}

	@Test
	public void addChildWorksForDecorators() throws Exception {
		db.addOrUpdateResourceType(OnOffSwitch.class);
		db.addResource("switch3", OnOffSwitch.class, getAppId());
		TreeElement el = db.getToplevelResource("switch3");
		int childCount = el.getChildren().size();
		TreeElement decorator = el.addChild("foo", IntegerResource.class, true);
		assertEquals(childCount + 1, el.getChildren().size());
		assertNotNull(decorator);
		assertEquals("foo", decorator.getName());
		// System.err.println(el.getChildren());
		for (TreeElement child : el.getChildren()) {
			if (child.getName().equals("foo") && child.getResID() == decorator.getResID()) {
				assertTrue(child.isDecorator());
				return;
			}
		}
		fail("decorator not found");
	}

	// actually TreeElement does not have to handle this, resource implementation must pass correct parameters
	@Test(expected = ResourceNotFoundException.class)
	public void addChildBarfsIfNoSuchOptionalElementExists() throws Exception {
		db.addOrUpdateResourceType(TEST_TYPE);
		db.addResource(TEST_RESOURCE_NAME, TEST_TYPE, getAppId());
		TreeElement el = db.getToplevelResource(TEST_RESOURCE_NAME);
		assertNotNull(el);
		el.addChild("noSuchChildR_esourc_e", OnOffSwitch.class, false);
	}

	// actually TreeElement does not have to handle this, resource implementation must pass correct parameters
	@Test(expected = ResourceNotFoundException.class)
	public void addChildBarfsIfTypeDoesNotMatch() throws Exception {
		db.addOrUpdateResourceType(OnOffSwitch.class);
		db.addOrUpdateResourceType(IntegerResource.class);
		db.addResource("switch4", OnOffSwitch.class, getAppId());
		TreeElement el = db.getToplevelResource("switch4");
		assertNotNull(el);
		el.addChild("state", IntegerResource.class, false);
	}

	@Test
	public void settingReferencesForOptionalWorks() throws Exception {
		db.addOrUpdateResourceType(ReferenceTesting.class);
		db.addOrUpdateResourceType(OnOffSwitch.class);
		db.addResource("ReferenceTest", ReferenceTesting.class, getAppId());
		TreeElement top = db.getToplevelResource("ReferenceTest");
		assertNotNull(top);
		TreeElement sw = top.addChild("switchA", OnOffSwitch.class, false);
		assertNotNull(sw);
		top.addReference(sw, "switchB", false);
		TreeElement ref = top.getChild("switchB");
		assertTrue(ref.isReference());
		assertEquals("switchB", ref.getName());
		assertTrue(ref.getReference() == sw);
	}

	@Test
	public void settingBooleanValueWorks() throws Exception {
		db.addOrUpdateResourceType(TEST_TYPE);
		TreeElement el = db.addResource("SetBooleanTest", TEST_TYPE, getAppId());
		TreeElement state = el.addChild("stateControl", BooleanResource.class, false);
		boolean value = state.getData().getBoolean();
		boolean newVal = !value;
		state.getData().setBoolean(newVal);
		assertEquals(newVal, state.getData().getBoolean());
	}

}
