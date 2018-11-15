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
package org.ogema.persistence.impl.mem;

import static org.junit.Assert.*;
import org.junit.Test;
import org.ogema.model.actors.OnOffSwitch;
import org.ogema.resourcetree.TreeElement;

/**
 * 
 * @author jlapp
 */
public class ResourceDBTest extends TestBase {

	@Test
	public void addResourceTypeWorks() throws Exception {
		db.addOrUpdateResourceType(TEST_TYPE);
		assertTrue(db.hasResourceType(TEST_TYPE.getCanonicalName()));
	}

	@Test
	public void addingSameResourceTypeAgainWorks() throws Exception {
		db.addOrUpdateResourceType(TEST_TYPE);
		assertTrue(db.hasResourceType(TEST_TYPE.getCanonicalName()));
		db.addOrUpdateResourceType(TEST_TYPE);
		assertTrue(db.hasResourceType(TEST_TYPE.getCanonicalName()));
	}

	@Test
	public void addResourceWorks() throws Exception {
		addResourceTypeWorks();
		String resourceName = resname();
		TreeElement el = db.addResource(resourceName, TEST_TYPE, getAppId());
		assertNotNull(el);
		assertEquals(el, db.getToplevelResource(resourceName));
		assertEquals(resourceName, el.getName());
		assertTrue(db.getAllToplevelResources().contains(el));
	}

	@Test
	public void deleteResourceWorks() throws Exception {
		db.addOrUpdateResourceType(OnOffSwitch.class);
		String resourceName = resname();
		TreeElement el = db.addResource(resourceName, OnOffSwitch.class, getAppId());
		assertEquals(el, db.getToplevelResource(resourceName));
		assertTrue(db.getAllToplevelResources().contains(el));
		db.deleteResource(el);
		assertFalse(db.getAllToplevelResources().contains(el));
	}

}
