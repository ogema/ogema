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
