/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3
 * as published by the Free Software Foundation.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
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
