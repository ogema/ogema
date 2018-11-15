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

public interface SourcesManagement {
	

	/**
	 * Returns a list of the currently connected app stores.
	 * 
	 * @return
	 */
	public List<ApplicationSource> getConnectedAppSources();
	
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
	 * Gets the default application source as it installed in system.
	 * 
	 * @return The default {@link ApplicationSource}
	 */
	public ApplicationSource getDefaultAppStore();

}
