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
import java.util.Random;

import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.model.locations.Building;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator, Application {
	public static boolean bundleIsRunning = true;
	private ServiceRegistration<Application> me;
	private boolean running;

	public void start(BundleContext bc) throws IOException {
		me = bc.registerService(Application.class, this, null);
	}

	public void stop(BundleContext bc) throws IOException {
		bundleIsRunning = false;
		me.unregister();
	}

	@Override
	public void start(final ApplicationManager appManager) {
		try {
			new OgemaFilePermissionTest(appManager);
		} catch (Throwable e1) {
			e1.printStackTrace();
		}
		try {
			new Zwave(appManager);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// new HMPowerboxTest(appManager.getResourceAccess());
		new Thread(new Runnable() {

			@Override
			public void run() {
				running = true;
				String smartPlugRes = "Smart_Plug_00124b00088dce49_01/onOffSwitch/stateControl";
				BooleanResource control = appManager.getResourceAccess().getResource(smartPlugRes);
				while (control == null) {
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
					}
					control = appManager.getResourceAccess().getResource(smartPlugRes);
				}
				control.setValue(true);
				Random rnd = new Random();
				while (running) {
					long millis = rnd.nextInt(100) * 6000; // 0...600000ms
					try {
						Thread.sleep(millis);
					} catch (InterruptedException e) {
					}
					boolean currentState = control.getValue();
					if (currentState) {
						control.setValue(false);
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
						}
						control.setValue(true);
					}
					else
						control.setValue(true);
				}
			}
		}).start();

		new Thread(new Runnable() {

			@Override
			public void run() {
				@SuppressWarnings("unchecked")
				ResourceList<Building> buildings = appManager.getResourceManagement()
						.createResource("persistencyOptimizationTestResource", ResourceList.class);
				buildings.setElementType(Building.class);
				int count = buildings.size();
				while (count++ < 3000) {
					buildings.add();
				}
			}
		}).start();
	}

	@Override
	public void stop(AppStopReason reason) {
		running = false;
	}

}
