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
package org.ogema.application.manager.impl;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Objects;

import org.ogema.core.application.AppID;
import org.ogema.core.application.Application;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleReference;

public class AppIDImpl implements AppID {

	/*
	 * Bundle reference of the owner application
	 */
	Bundle bundle;
	/*
	 * Owner application reference
	 */
	Application app;
	/*
	 * Id string
	 */
	String id;

	@Override
	public String getIDString() {
		return id;
	}

	static AppIDImpl getNewID(Application app) {
		AppIDImpl result = null;
		ClassLoader cl = app.getClass().getClassLoader();
		if (cl instanceof BundleReference) {
			result = new AppIDImpl();
			BundleReference ref = (BundleReference) cl;
			result.bundle = ref.getBundle();
			result.app = app;
			result.id = createID(ref.getBundle(), app);
		}
		return result;
	}

	private static String createID(Bundle bundle, Application app) {
		String loc = bundle.getLocation();
		int begin = loc.lastIndexOf('/') + 1;
		int end = loc.lastIndexOf('.');
		if (begin > end)
			end = loc.length();
		String appName = app.getClass().getSimpleName();
		// return String.format("[%d]_%s@%d/%s", instanceCounter.incrementAndGet(), appName, bundle.getBundleId(), loc
		// .substring(begin, end));// loc.substring(begin, end) + "_" + bundle.getBundleId();
		return String.format("%s@%d/%s", appName, bundle.getBundleId(), loc.substring(begin, end));
	}

	static AppIDImpl getNewID(Bundle b, Application app) {
		AppIDImpl result = null;
		result = new AppIDImpl();
		result.bundle = b;
		result.app = app;
		result.id = createID(b, app);
		return result;
	}

	@Override
	public String getLocation() {
		return AccessController.doPrivileged(new PrivilegedAction<String>() {
			public String run() {
				return bundle.getLocation();
			}
		});
	}

	@Override
	public Bundle getBundle() {
		// TODO check AdminPermission
		return bundle;
	}

	@Override
	public Application getApplication() {
		// TODO check AdminPermission
		return app;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 43 * hash + Objects.hashCode(this.id);
		return hash;
	}

	public boolean equals(Object o) {
		if (!(o instanceof AppID))
			return false;
		AppID id = (AppID) o;
		if (id.getIDString().equals(this.getIDString()))
			return true;
		return false;
	}

	@Override
	public String toString() {
		return "AppIDImpl{" + "bundle=" + bundle + ", app=" + app + '}';
	}

}
