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
