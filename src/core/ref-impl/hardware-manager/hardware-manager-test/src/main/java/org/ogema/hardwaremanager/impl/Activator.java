package org.ogema.hardwaremanager.impl;

import org.ogema.hardwaremanager.HardwareManager;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * BundleActivator required by the OSGi framework.
 * 
 * @author pau
 * 
 */
public class Activator implements BundleActivator {

	private ServiceReference<HardwareManager> hardwaremanagerReference;
	private HardwareManager hardwaremanager;

	@Override
	public void start(BundleContext bcontext) throws Exception {
		System.err.println("Start hardwaremanager-test");

		// Get Hardwaremanager service
		hardwaremanagerReference = bcontext.getServiceReference(HardwareManager.class);
		hardwaremanager = bcontext.getService(hardwaremanagerReference);

		Test.testHardwareManagerImpl(hardwaremanager);

	}

	@Override
	public void stop(BundleContext bcontext) throws Exception {
		System.err.println("Stop hardwaremanager-test");
		bcontext.ungetService(hardwaremanagerReference);
	}
}
