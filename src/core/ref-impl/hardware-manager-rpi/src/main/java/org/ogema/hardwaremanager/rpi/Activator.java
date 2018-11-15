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
package org.ogema.hardwaremanager.rpi;

import org.ogema.hardwaremanager.api.NativeAccess;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * BundleActivator required by the OSGi framework.
 * 
 * @author pau
 * 
 */
public class Activator implements BundleActivator {

	private ServiceRegistration<NativeAccess> nativeAccessRegistration;
	private NativeAccess nativeAccess;

	@Override
	public void start(BundleContext bcontext) throws Exception {
		final String osName = System.getProperty("os.name");
		if (osName.startsWith("Windows"))
			return;
		nativeAccess = new NativeAccessImpl();
		nativeAccessRegistration = bcontext.registerService(NativeAccess.class, nativeAccess, null);
	}

	@Override
	public void stop(BundleContext bcontext) throws Exception {
		if (nativeAccessRegistration != null)
			nativeAccessRegistration.unregister();
		nativeAccessRegistration = null;
		nativeAccess = null;
	}
}
