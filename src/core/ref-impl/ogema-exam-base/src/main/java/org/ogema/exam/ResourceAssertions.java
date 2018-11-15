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
package org.ogema.exam;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.ogema.core.model.Resource;

public class ResourceAssertions {

	public static void assertExists(Resource res) {
		assertTrue(toString(res) + " should exists", res.exists());
	}

	public static void assertInactive(Resource res) {
		assertFalse(toString(res) + " should be inactive", res.isActive());
	}

	public static String toString(Resource res) {
		return res.getPath();
	}

	public static void assertDeleted(Resource res) {
		assertFalse(toString(res) + " should be deleted", res.exists());
	}

	public static void assertActive(Resource res) {
		assertTrue(toString(res) + " should be active", res.isActive());
	}

	/** res is direct subresource, not a reference
	 * @param res */
	public static void assertDirectSubresource(Resource res) {
		assertFalse(toString(res) + " should be a direct subresource", res.isReference(false));
	}

	public static void assertReference(Resource res) {
		assertTrue(toString(res) + " should be a reference", res.isReference(false));
	}

	public static void assertReferencePath(Resource res) {
		assertTrue(toString(res) + " should be a path containing a reference", res.isReference(true));
	}

	public static void assertIsVirtual(Resource res) {
		assertFalse(toString(res) + " should not exist", res.exists());
	}

	public static void assertLocationsEqual(Resource res1, Resource res2) {
		assertTrue("Resources should have the same locations: " + res1 + ", " + res2,res1.equalsLocation(res2));
	}
	
}
