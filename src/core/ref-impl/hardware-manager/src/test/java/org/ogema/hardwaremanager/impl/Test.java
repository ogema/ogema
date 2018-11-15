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

import java.util.Map;

import org.ogema.core.hardwaremanager.HardwareDescriptor;
import org.ogema.core.hardwaremanager.HardwareDescriptor.HardwareType;
import org.ogema.core.hardwaremanager.HardwareListener;
import org.ogema.core.hardwaremanager.HardwareManager;
import org.ogema.core.hardwaremanager.SerialHardwareDescriptor;
import org.ogema.core.hardwaremanager.UsbHardwareDescriptor;
import org.ogema.hardwaremanager.api.NativeAccess;

public class Test {

	private static NativeAccess nativeAccess;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		System.out.println(System.getProperty("java.library.path"));
		System.out.println(System.getProperty("os.name"));
		System.out.println(System.getProperty("os.arch"));

		// nativeAccess = new NativeAccessImpl();

		// testNativeAccess();
		testHardwareManagerImpl();
	}

	private static void testHardwareManagerImpl() {
		HardwareManager manager = new HardwareManagerImpl(nativeAccess);

		testHardwareManagerIdentifiers(manager);

		testHardwareManagerDescriptors(manager);

		testHardwareManagerEvents(manager);

		((HardwareManagerImpl) manager).exit();
	}

	private static void testHardwareManagerDescriptors(HardwareManager manager) {
		for (HardwareDescriptor descriptor : manager.getHardwareDescriptors()) {
			System.out.println("type " + descriptor.getHardwareType() + " id " + descriptor.getIdentifier());

			if (descriptor.getHardwareType() == HardwareType.SERIAL) {
				System.out.println("Port " + ((SerialHardwareDescriptor) descriptor).getPortName());
			}

			if (descriptor.getHardwareType() == HardwareType.USB) {
				System.out.println("Port " + ((UsbHardwareDescriptor) descriptor).getPortName());
				for (Map.Entry<String, String> entry : ((UsbHardwareDescriptor) descriptor).getInfo().entrySet()) {
					System.out.println(entry);
				}
			}
		}
	}

	private static void testHardwareManagerIdentifiers(HardwareManager manager) {
		for (String id : manager.getHardwareIdentifiers()) {
			System.out.println(id);
		}
	}

	private static void testHardwareManagerEvents(HardwareManager manager) {
		HardwareListener listener = new HardwareListener() {

			@Override
			public void hardwareRemoved(HardwareDescriptor descriptor) {
				System.out.println("Removed " + descriptor.getIdentifier());

			}

			@Override
			public void hardwareAdded(HardwareDescriptor descriptor) {
				System.out.println("Added " + descriptor.getIdentifier());

			}
		};

		manager.addListener(listener);

		try {
			Thread.sleep(20 * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	// private static void testNativeAccess() {
	// Container[] handles = nativeAccess.getHandles();
	//
	// printContainerArray(handles);
	// printIds(handles);
	// printPortStrings(handles);
	// printUsbInfos(handles);
	// printEvents();
	// }

	// private static void printEvents() {
	// Container event = new Container();
	//
	// for (int i = 0; i < 4; i++) {
	// event = nativeAccess.getEvent(event);
	// printContainer(event);
	// }
	// }
	//
	// private static void printContainer(Container event) {
	// System.out.println(event);
	//
	// }
	//
	// private static void printUsbInfos(Container[] handles) {
	// for (Container container : handles) {
	// if (container.getType() == HardwareType.USB)
	// System.out.println(nativeAccess.getUsbInfo(container.handle));
	// else
	// System.out.println("!Skip Entry with Type " + container.getType());
	// }
	// }
	//
	// private static void printPortStrings(Container[] handles) {
	// for (Container container : handles) {
	// System.out.println(nativeAccess.getPortString(container.handle));
	// }
	// }
	//
	// private static void printIds(Container[] handles) {
	// for (Container container : handles) {
	// System.out.println(nativeAccess.getIdString(container.handle));
	// }
	// }
	//
	// private static void printContainerArray(Container[] handles) {
	// for (Container container : handles) {
	// System.out.println("Type: " + container.getType() + ". String:" + (String) container.handle);
	// }
	// }
	//
}
