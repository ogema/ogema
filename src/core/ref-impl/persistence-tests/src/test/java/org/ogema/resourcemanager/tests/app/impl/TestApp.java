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
package org.ogema.resourcemanager.tests.app.impl;

import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.resourcemanager.tests.custom.TypeTestResource;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * Exactly like TestApp, except that it does not create the optional list subresource
 * of TypeTestResource.
 */
public class TestApp implements BundleActivator, Application {
	
	public final static String RESOURCE_NAME = "typeTestResourceGlobal";
	
	private TypeTestResource testResource;
	private volatile ServiceRegistration<Application> ownReg;
	
	@Override
	public void start(BundleContext context) throws Exception {
		ownReg = context.registerService(Application.class, this, null);
	}
	
	@Override
	public void stop(BundleContext context) throws Exception {
		final ServiceRegistration<Application> ownReg = this.ownReg;
		this.ownReg = null;
		if (ownReg != null) {
			try {
				ownReg.unregister();
			} catch (Exception ignore) {}
		}
	}

	@Override
	public void start(ApplicationManager appManager) {
		testResource = appManager.getResourceManagement().createResource(RESOURCE_NAME, TypeTestResource.class);
		System.out.println("Test resource created, without subresource: " + testResource);
	};
	
	@Override
	public void stop(AppStopReason reason) {
		testResource = null;
	}
	
}
