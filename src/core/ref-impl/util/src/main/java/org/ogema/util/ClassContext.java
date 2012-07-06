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
package org.ogema.util;

import java.security.AccessController;
import java.security.PrivilegedAction;

import org.ogema.core.application.Application;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleReference;

/**
 * TODO Do we need this? Depends on how we implement security.
 */
@SuppressWarnings("rawtypes")
//use of Class[]
public class ClassContext extends SecurityManager {

	static ClassContext instance = null;

	public static ClassContext getInstance() {
		if (instance == null) {
			instance = new ClassContext();
		}
		return instance;
	}

	Bundle getCallerBudle() {
		Class[] clss = getClassesOnCallStack();
		for (Class<?> cls : clss) {
			ClassLoader cl = cls.getClassLoader();
			if (cl instanceof BundleReference) {
				BundleReference ref = (BundleReference) cl;
				Bundle b = ref.getBundle();
				return b;
			}
		}
		return null;
	}

	public Bundle getBudleByClass(Class cls) {
		ClassLoader cl = cls.getClassLoader();
		if (cl instanceof BundleReference) {
			BundleReference ref = (BundleReference) cl;
			Bundle b = ref.getBundle();
			return b;
		}
		return null;
	}

	/*
	 * get the classes on the call stack the first element of the result is always ogema.internal.GetClassContext!
	 */
	public Class[] getClassesOnCallStack() {
		return this.getClassContext();
	}

	public String getAppUserId() {
		Class[] clss = getClassesOnCallStack();
		for (Class<?> cls : clss) {
			ClassLoader cl = cls.getClassLoader();
			if (cl instanceof BundleReference) {
				BundleReference ref = (BundleReference) cl;
				Bundle b = ref.getBundle();
				return b.getLocation();
			}
		}
		return null;
	}

	public ClassLoader getCallingAppClassLoader() {
		return (ClassLoader) AccessController.doPrivileged(new PrivilegedAction<Object>() {
			Class[] clazzes = ClassContext.instance.getClassesOnCallStack();

			@Override
			public Object run() {
				for (Class clazz : clazzes) {
					if (Application.class.isAssignableFrom(clazz)) {
						System.out.println(clazz + " --> is of type Application!");
						return clazz.getClassLoader();
					}
				}
				return null;
			}
		});
	}

	static {
		AccessController.doPrivileged(new PrivilegedAction<Object>() {
			@Override
			public Object run() {
				instance = new ClassContext();
				return instance;
			}
		});
	}
}
