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
