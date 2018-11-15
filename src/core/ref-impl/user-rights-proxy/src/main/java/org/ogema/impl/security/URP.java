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
package org.ogema.impl.security;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Service;
import org.ogema.accesscontrol.UserRightsProxy;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.LoggerFactory;

@Component(specVersion = "1.1", immediate = true)
@Service( { UserRightsProxy.class, Application.class })
public class URP implements UserRightsProxy, Application {

	String name;
	//	@Reference
	//	PermissionManager permMan;
	Bundle b;

	@Activate
	protected void activate(BundleContext context) throws Exception {
		this.b = context.getBundle();
		String loc = context.getBundle().getLocation();
		int i = loc.lastIndexOf(':');
		name = loc.substring(i + 1);
		LoggerFactory.getLogger(getClass()).info("activated URP for user '{}'", name);
	}

	@Deactivate
	protected void deactivate(BundleContext context) throws Exception {
	}

	@Override
	public String getUserName() {
		return name;
	}

	@Override
	public String toString() {
		return "User rights proxy: " + name;
	}

	@Override
	public void start(ApplicationManager appManager) {
	}

	@Override
	public void stop(AppStopReason reason) {
	}

	@Override
	public Bundle getBundle() {
		return b;
	}

}
