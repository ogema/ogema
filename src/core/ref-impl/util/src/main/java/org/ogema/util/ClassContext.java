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
