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
package org.ogema.exam;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.ogema.core.model.Resource;

public class ResourceAssertions {

	public static void assertExists(Resource res) {
		assertTrue(toString(res) + " should exists", res.exists());
	}

	public static void assertInactive(Resource res) {
		assertFalse(toString(res) + " should be inactive", res.exists());
	}

	public static String toString(Resource res) {
		return res.getPath();
	}

	public static void assertDeleted(Resource res) {
		assertFalse(toString(res) + " should be deleted", res.exists());
	}

	public static void assertActive(Resource res) {
		assertFalse(toString(res) + " should be active", res.exists());
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

}
