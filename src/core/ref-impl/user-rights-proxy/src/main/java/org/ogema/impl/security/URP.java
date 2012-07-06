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
package org.ogema.impl.security;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.ogema.accesscontrol.PermissionManager;
import org.ogema.accesscontrol.UserRightsProxy;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

@Component(specVersion = "1.1", immediate = true)
@Service( { UserRightsProxy.class, Application.class })
public class URP implements UserRightsProxy, Application {

	String name;
	@Reference
	PermissionManager permMan;
	Bundle b;

	@Activate
	protected void activate(BundleContext context) throws Exception {
		this.b = context.getBundle();
		String loc = context.getBundle().getLocation();
		int i = loc.lastIndexOf('/');
		name = loc.substring(i + 1 + 3);
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
