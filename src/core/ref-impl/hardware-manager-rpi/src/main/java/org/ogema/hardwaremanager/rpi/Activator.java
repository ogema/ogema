/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
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

		nativeAccess = new NativeAccessImpl();
		nativeAccessRegistration = bcontext.registerService(NativeAccess.class, nativeAccess, null);
	}

	@Override
	public void stop(BundleContext bcontext) throws Exception {
		nativeAccessRegistration.unregister();
	}
}
