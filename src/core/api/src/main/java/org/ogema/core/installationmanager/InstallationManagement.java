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
package org.ogema.core.installationmanager;

import java.util.List;

import org.ogema.core.security.AppPermission;
import org.osgi.framework.Bundle;

/**
 * Framework service for installation of applications and drivers and management of connections to application sources
 */
public interface InstallationManagement {
	/**
	 * Connect to AppStore or to local directory containing apps.
	 * 
	 * @param address
	 *            String representation of a valid url to the app store or local directory containing apps
	 * @return ApplicationSource object that represent the connected app store.
	 */
	public ApplicationSource connectAppSource(String address);

	/**
	 * Disconnect from a remote or local app store.
	 * 
	 * @param address
	 *            the address information that was used to connect to the app store.
	 */
	public void disconnectAppSource(String address);

	/**
	 * Returns a list of the currently connected app stores.
	 * 
	 * @return
	 */
	public List<ApplicationSource> getConnectedAppSources();

	/**
	 * Install the App by registering it in OGEMA and installing and starting its relevant bundle in the OSGi framework.
	 * The way how the bundle file is transfered is determined by the responsible ApplicationSource. The OSGi
	 * Conditional Permission Admin is updated with the permission set to be assigned via
	 * {@link InstallableApplication#setGrantedPermissions(AppPermission)}.
	 * 
	 * @param app
	 *            The object holding all relevant information to install the application
	 */
	public void install(InstallableApplication app);

	/**
	 * Create an InstallableApplication instance with the given address and name information. The returned object is
	 * initialized with the given arguments. Other properties like the granted permissions have to be initialized before
	 * the application could be installed.
	 * 
	 * @param address
	 *            URL string which is the location of the source of the application to install.
	 * @param name
	 *            The name of the bundle file of the application.
	 * @return The initialized object InstallableApplication
	 */
	public InstallableApplication createInstallableApp(String address, String name);

	/**
	 * Creates an InstallableApplication object to an already installed application. This can be user to initiate an
	 * update of the installed application.
	 * 
	 * @param b
	 *            The bundle reference of the application
	 * @return A new instance of InstallableApplication which is initialized with the bundles data
	 */
	public InstallableApplication createInstallableApp(Bundle b);

	/**
	 * Gets the default application source as it installed in system.
	 * 
	 * @return The default {@link ApplicationSource}
	 */
	public ApplicationSource getDefaultAppStore();

	/**
	 * Gets the name of the bundle store, that is provided by the framework.
	 * 
	 * @return The locale store or null if the framework doesn't support such a store.
	 */
	public ApplicationSource getLocalStore();

}
