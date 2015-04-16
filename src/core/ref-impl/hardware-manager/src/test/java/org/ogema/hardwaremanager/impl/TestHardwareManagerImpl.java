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
package org.ogema.hardwaremanager.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Ignore;
import org.junit.Test;
import org.ogema.core.hardwaremanager.HardwareDescriptor.HardwareType;

public class TestHardwareManagerImpl {
	@Test
	public void testConstructor() {
		// start Hardwaremanager with no devices
		NativeAccessTest nativeAccess = new NativeAccessTest();
		@SuppressWarnings("unused")
		HardwareManagerImpl manager = new HardwareManagerImpl(nativeAccess);
	}

	@Ignore
	@Test
	public void testConstructorWithOneDevice() {
		NativeAccessTest nativeAccess = new NativeAccessTest();

		NativeDescriptor descriptor = new NativeDescriptor();
		descriptor.identifier = "id1";
		descriptor.port = "port1";
		descriptor.type = HardwareType.SERIAL;

		nativeAccess.addHandle(descriptor);

		HardwareManagerImpl manager = new HardwareManagerImpl(nativeAccess);

		Collection<String> ids = manager.getHardwareIdentifiers();

		assertTrue(1 == ids.size());

		for (String id : ids) {
			assertEquals(id, "id1");
		}
	}

	@Ignore
	@Test
	public void testConstructorWithTwoDevice() {
		NativeAccessTest nativeAccess = new NativeAccessTest();
		nativeAccess.addHandle(new NativeDescriptor("id1", HardwareType.SERIAL, "port1", null));
		nativeAccess.addHandle(new NativeDescriptor("id2", HardwareType.SERIAL, "port2", null));
		nativeAccess.addHandle(new NativeDescriptor("id3", HardwareType.SERIAL, "port3", null));
		nativeAccess.addHandle(new NativeDescriptor("id4", HardwareType.SERIAL, "port4", null));

		HardwareManagerImpl manager = new HardwareManagerImpl(nativeAccess);

		Collection<String> ids = manager.getHardwareIdentifiers();

		assertTrue(1 == ids.size());

		for (String id : ids) {
			assertEquals(id, "id1");
		}
	}
}
