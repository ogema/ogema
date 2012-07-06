package org.ogema.hardwaremanager.impl;

import java.util.Map;

import org.ogema.hardwaremanager.HardwareDescriptor;
import org.ogema.hardwaremanager.HardwareDescriptor.HardwareType;
import org.ogema.hardwaremanager.HardwareListener;
import org.ogema.hardwaremanager.HardwareManager;
import org.ogema.hardwaremanager.SerialHardwareDescriptor;
import org.ogema.hardwaremanager.UsbHardwareDescriptor;

public class Test {

	public static void testHardwareManagerImpl(HardwareManager manager) {

		testHardwareManagerIdentifiers(manager);

		testHardwareManagerDescriptors(manager);

		testHardwareManagerEvents(manager);

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
}
