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

import java.util.List;

import org.ogema.core.application.AppID;
import org.ogema.core.security.AppPermission;
import org.osgi.framework.Bundle;

/**
 * Application offered by an application source. Installable applications are usually not installed yet.
 * Permission information must be verified by the framework during the installation process.
 */
public interface InstallableApplication {

	enum InstallState {
		APPCHOSEN, DESIRED_PERMS_SENT, RECEIVE_GRANTED, BUNDLE_INSTALLING, BUNDLE_INSTALLED, APP_INSTALLED, ABORTED, FINISHED
	}

	/**
	 * Gets the name of the application.
	 * 
	 * @return The name string
	 */
	public String getName();

	/**
	 * Gets description that may contain HTML-tags.
	 * 
	 * @return The description string
	 */
	public String getDescription();

	/**
	 * Gets link to web presentation of the application outside the framework. This link should lead to a full web site
	 * and should be opened in a new browser window/tab.
	 * 
	 * @return URI of application/distributor website containing relevant information
	 */
	public String getPresentationURI();

	/**
	 * Gets link to an HTML snippet that can be integrated into a marketplace application.
	 * 
	 * @return URI for obtaining the HTML snippet (including possible references to further elements to be loaded)
	 */
	public String getPresentationSnippetURI();

	/**
	 * Gets all of the OSGi specific permissions requested by the App. These are listed in OSGI-INF/permissions.perm
	 * file.
	 * 
	 * @return A List of AppPermission where each of them represents an entry in the permissions.perm file of the App.
	 */
	public List<String> getPermissionDemand();

	/**
	 * Gets the AppPermission object which hold a set of permissions that are to be granted to the application.
	 * 
	 * @return The AppPermission object
	 */
	public AppPermission getGrantedPermissions();

	/**
	 * Sets the AppPermission object which hold a set of permissions that are to be granted to the application.
	 * 
	 * @param perms
	 *            The new granted permissions
	 */
	public void setGrantedPermissions(AppPermission perms);

	/**
	 * Gets the location of the application which is installable.
	 * 
	 * @return The location string
	 */
	public String getDirectory();

	/**
	 * Sets state of the application. Since the installation process of the application consists of more than one step,
	 * the state information is needed for decision making by the InstallationManagement.
	 * 
	 * @param state
	 *            The current state information of the installabel app.
	 */
	public void setState(InstallState state);

	/**
	 * Gets the current state information of the application. @see InstallableApplication#setState
	 * 
	 * @return Current state information
	 */
	public InstallState getState();

	/**
	 * Gets the AppID object of the installable application. This may be null in dependence of the
	 * {@link InstallableApplication.InstallState}
	 * 
	 * @return The AppID object
	 */
	public AppID getAppid();

	/**
	 * Sets the AppID object. @see #getAppid
	 * 
	 * @param appid
	 *            The AppID object
	 */
	public void setAppid(AppID appid);

	/**
	 * Gets the location string as it expected as osgi bundle location string
	 * 
	 * @return The location string
	 */
	public String getLocation();

	/**
	 * Sets the reference of the Bundle that belongs to the application.
	 * 
	 * @param b
	 *            The bundle reference
	 */
	public void setBundle(Bundle b);

	/**
	 * Gets the bundle reference that belongs to this application. This may be null in dependence of the
	 * {@link InstallableApplication.InstallState}
	 * 
	 * @return The bundle reference
	 */
	public Bundle getBundle();

	/**
	 * Prepares the application for the installation process.
	 */
	public void prepare();
}
