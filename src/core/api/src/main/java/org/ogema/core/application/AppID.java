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
/**
 * 
 */
package org.ogema.core.application;

import java.net.URL;

import javax.servlet.http.HttpSession;

import org.ogema.core.security.AppPermissionType.AdminAction;
import org.osgi.framework.Bundle;

/**
 * This interface provides methods that help to identify the ogema application which is the owner of this AppID. It
 * supports a reference to the osgi context representation of the application too.
 */
public interface AppID {
	/**
	 * Get a unique string as an id for this application. Note: this is not invariant under restart of the framework,
	 * and it also changes when the application is removed and reinstalled. For an invariant app id, use the bundle
	 * symbolic name (see {@link #getBundle()}).
	 * 
	 * @return Id string of the owner application or null if any exception occurs.
	 */
	public String getIDString();

	/**
	 * Get the location identifier string of the application. This is equal to the applications bundle location
	 * identifier string.
	 * 
	 * @return Application location identifier string or null if any exception occurs.
	 */
	public String getLocation();

	/**
	 * Returns the bundle reference that contains this application.
	 * 
	 * @return Applications bundle reference
	 */
	public Bundle getBundle();

	/**
	 * Returns the application reference which is associated with the owner application of this AppID or null if the
	 * caller doesn't have the appropriate permission with {@link AdminAction#APP}.
	 * 
	 * @return Owners application reference.
	 */
	public Application getApplication();

	/**
	 * Gets the name of the owner user.
	 *
	 * @return the name string
	 */
	public String getOwnerUser();

	/**
	 * Gets the name of the group, the owner user is a member of it.
	 *
	 * @return the name string
	 */
	public String getOwnerGroup();

	/**
	 * Gets the version string of the app.
	 *
	 * @return the name string
	 */
	public String getVersion();

	public URL getOneTimePasswordInjector(String path, HttpSession ses);
	
	/**
	 * Is the app currently active?
	 * @return
	 * 		true, until app's stop method has been executed (successfully or not), 
	 * 		false afterwards.
	 */
	public boolean isActive();
}
