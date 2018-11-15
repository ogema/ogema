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

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.ogema.applicationregistry.ApplicationListener;
import org.ogema.applicationregistry.ApplicationRegistry;
import org.ogema.core.administration.AdminApplication;
import org.ogema.core.application.AppID;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleReference;

/**
 *
 * @author jlapp, Zekeriya Mansuroglu
 */
@Component
@Service(ApplicationRegistry.class)
@Reference(name = "applications", referenceInterface = AdminApplication.class, cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE, policy = ReferencePolicy.DYNAMIC, bind = "addAdminApplication", unbind = "removeAdminApplication")
public class ApplicationRegistryImpl extends SecurityManager implements ApplicationRegistry { // XXX

	/**
	 * List of installed apps by app id string
	 */
	private final Map<String, AdminApplication> appAcc = new HashMap<>();
	/**
	 * List of installed apps by the bundle reference
	 */
	// FIXME: there can be more than 1 app per bundle
	private final Map<Bundle, AppID> appsByBundles = new HashMap<>();

	private final List<ApplicationListener> appListeners = new ArrayList<>();
	
	@Deactivate
	protected synchronized void deactivate(Map<String,Object> props) {
		appsByBundles.clear();
		appListeners.clear();
		appListeners.clear();
		// at this point we do no longer dispatch any events
	}

	synchronized protected void addAdminApplication(AdminApplication aa) {
		AppID appid = aa.getID();
		String appIDString = appid.getIDString();

		// Add app to the list of installed apps
		appAcc.put(appIDString, aa);
		appsByBundles.put(aa.getBundleRef(), appid);
		dispatchAddEvent(appid);
	}

	private void dispatchAddEvent(AppID appid) {
		for (ApplicationListener al : appListeners) {
			try {
				al.appInstalled(appid);
			} catch (Throwable e) {
			}
		}
	}

	synchronized protected void removeAdminApplication(AdminApplication aa) {
		AppID id = aa.getID();
		String appIDString = id.getIDString();
		appAcc.remove(appIDString);
		dispatchRemoveEvent(id);
	}

	private void dispatchRemoveEvent(AppID appid) {
		for (ApplicationListener al : appListeners) {
			try {
				al.appRemoved(appid);
			} catch (Throwable e) {
			}
		}
	}

	@Override
	public synchronized AppID getAppByBundle(Bundle b) {
		return appsByBundles.get(b);
	}

	@Override
	public synchronized AdminApplication getAppById(String id) {
		AdminApplication aaa = appAcc.get(id);
		return aaa;
	}

	@Override
	synchronized public List<AdminApplication> getAllApps() {
		return new ArrayList<>(appAcc.values());
	}

	/*
	 * Get the first occurrence of a class that belongs to an ogema application. All entries up to the class parameter
	 * are ignored. (non-Javadoc)
	 * 
	 * @see org.ogema.core.administration.AdministrationManager#getContextApp(java.lang.Class)
	 */
	@Override
	public AppID getContextApp(Class<?> ignore) {
		AppID result;
		final Class<?> param = ignore;
		result = AccessController.doPrivileged(new PrivilegedAction<AppID>() {
			@Override
			public AppID run() {
				/*
				 * get the classes on the call stack, the first element of the result is always class of this. All
				 * entries from top of stack until first occurence of the class given as parameter are skipped.
				 */
				Class<?>[] clss = getClassContext();
				int index = 0;
				for (Class<?> cls : clss) {
					clss[index++] = null;
					if (cls == param)
						break;
				}
				for (Class<?> cls : clss) {
					if (cls != null) {
						ClassLoader cl = cls.getClassLoader();
						if (cl instanceof BundleReference) {
							BundleReference ref = (BundleReference) cl;
							Bundle b = ref.getBundle();
							AppID id = appsByBundles.get(b);
							if (id != null)
								return id;
						}
					}
				}
				return null;
			}
		});
		return result;
	}

	@Override
	public Bundle getContextBundle(Class<?> ignore) {
		Bundle result;
		final Class<?> param = ignore;
		result = AccessController.doPrivileged(new PrivilegedAction<Bundle>() {
			@Override
			public Bundle run() {
				/*
				 * get the classes on the call stack, the first element of the result is always class of this. All
				 * entries from top of stack until first occurence of the class given as parameter are skipped.
				 */
				Class<?>[] clss = getClassContext();
				int index = 0;
				for (Class<?> cls : clss) {
					clss[index++] = null;
					if (cls == param)
						break;
				}
				for (Class<?> cls : clss) {
					if (cls != null) {
						ClassLoader cl = cls.getClassLoader();
						if (cl instanceof BundleReference) {
							BundleReference ref = (BundleReference) cl;
							Bundle b = ref.getBundle();
							return b;
						}
					}
				}
				return null;
			}
		});
		return result;
	}

	@Override
	public void registerAppListener(ApplicationListener al) {
		appListeners.add(al);
	}

	@Override
	public void unregisterAppListener(ApplicationListener al) {
		appListeners.remove(al);
	}
}
