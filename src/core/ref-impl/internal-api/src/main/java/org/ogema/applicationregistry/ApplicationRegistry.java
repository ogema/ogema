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
package org.ogema.applicationregistry;

import java.util.List;
import org.ogema.core.administration.AdminApplication;
import org.ogema.core.application.AppID;
import org.osgi.framework.Bundle;

/**
 *
 * @author jlapp
 */
public interface ApplicationRegistry { // XXX

	/**
	 * Get the AppID object related to the ogema application with the specified bundle reference.
	 * 
	 * @param b
	 *            The bundle reference of the application.
	 * @return The AppID object
	 * 
	 * @see org.ogema.core.administration.AdministrationManager#getAppByBundle(Bundle)
	 */
	public AppID getAppByBundle(Bundle b);

	/**
	 * Get the AppAdminAccess object related to the specified ogema application. In order to get get access the caller
	 * needs org.ogema.accesscontrol.AdminPermission.
	 * 
	 * @param id
	 *            Application id string
	 * @return AppAdminAccess object
	 * 
	 * @see org.ogema.core.administration.AdministrationManager#getAppById(String)
	 */
	public AdminApplication getAppById(String id);

	/**
	 * Get all apps installed.
	 *
	 * @see org.ogema.core.administration.AdministrationManager#getAllApps()
	 * 
	 */
	public List<AdminApplication> getAllApps();

	/**
	 * Get the first occurrence of a class that belongs to an ogema application. All entries up to the class parameter
	 * are ignored.
	 * 
	 * @see org.ogema.core.administration.AdministrationManager#getContextApp(java.lang.Class)
	 */
	public AppID getContextApp(Class<?> ignore);

	/**
	 * Get the first occurrence of a class that belongs to an osgi bundle. All entries up to the class parameter are
	 * ignored.
	 * 
	 * @see org.ogema.core.administration.AdministrationManager#getContextApp(java.lang.Class)
	 */
	public Bundle getContextBundle(Class<?> ignore);

	/**
	 * Register given ApplicationListener instance for future changes in ApplicationRegistry.
	 * 
	 * @param al
	 *            The ApplicationListener instance.
	 */
	public void registerAppListener(ApplicationListener al);
	
	/**
	 * Unregister given ApplicationListener instance.
	 * 
	 * @param al
	 *            The ApplicationListener instance.
	 */
	public void unregisterAppListener(ApplicationListener al);
}
