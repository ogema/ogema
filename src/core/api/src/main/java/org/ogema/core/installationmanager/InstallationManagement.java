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
package org.ogema.core.installationmanager;

import org.ogema.core.security.AppPermission;
import org.osgi.framework.Bundle;

/**
 * Framework service for installation of applications and drivers and management of connections to application sources
 */
public interface InstallationManagement {



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
	 * the application could be installed. Only file: URI's are supported as address.
	 * 
	 * @param address
	 *            URI string which is the location of the source of the application to install.
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
}
