/**
 * Copyright 2009 - 2014
 *
 * Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
 *
 * Fraunhofer IIS
 * Fraunhofer ISE
 * Fraunhofer IWES
 *
 * All Rights reserved
 */
package org.ogema.app.securityconsumer;

import java.io.IOException;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
//import org.ogema.app.securityprovider.Access;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.model.devices.sensoractordevices.SensorDevice;
import org.ogema.model.locations.Room;

public class Activator implements BundleActivator, Application {
	// Access us;

	public void start(BundleContext bc) throws IOException {
		bc.registerService(Application.class, this, null);
		// ServiceReference<?> sRef = bc.getServiceReference(Access.class.getName());
		// if (sRef != null) {
		// us = (Access) bc.getService(sRef);
		// if (us != null) {
		//
		// Thread run = new Thread(new Runnable() {
		//
		// @Override
		// public void run() {
		// // Action trough a privileged action by the service provider.
		// // The app doesn't need to have a permission for that.
		// us.login("admin");
		//
		// // The app must have a permission to make the following actions.
		// // The service provider (e.g. ResourceManager) uses the
		// // PermissionManager to make the
		// // checks needed.
		//
		// // SecurityException expected
		// boolean exception = false;
		// try {
		// us.getResource("type=*,path=/top1/sub1");
		// } catch (Exception e) {
		// exception = true;
		// }
		// if (exception) {
		// System.out.println("!FAILED! SecurityException shouldn't be thrown now. Test failed!");
		// throw new RuntimeException();
		// }
		//
		// us.getResourcesOfType(SensorDevice.class.getName());
		// us.getChannel("busid=*,devaddr=11 16 22,chaddr=12 176 201");
		//
		// }
		// });
		// run.start();
		// }
		// bc.ungetService(sRef);
		// }
	}

	public void stop(BundleContext bc) throws IOException {
		// ServiceReference<?> sRef = bc.getServiceReference(Access.class.getName());
		// if (sRef != null) {
		// Access us = (Access) bc.getService(sRef);
		// if (us != null) {
		// us.logout();
		// }
		// bc.ungetService(sRef);
		// }
	}

	@Override
	public void start(ApplicationManager appManager) {
		Room r = appManager.getResourceManagement().createResource("ExamProbe", Room.class);
	}

	@Override
	public void stop(AppStopReason reason) {
		// TODO Auto-generated method stub

	}

}
