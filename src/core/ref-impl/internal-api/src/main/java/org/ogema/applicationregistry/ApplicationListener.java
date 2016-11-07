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

import org.ogema.core.application.AppID;

/**
 * An implementation of this interface can be registered on {@link #ApplicationRegistry} to get event call backs whwn an
 * application is installed or removed.
 *
 */
public interface ApplicationListener {
	/**
	 * Call back method that signals an installed application.
	 * 
	 * @param app
	 *            The {@link #AppID} object of the installed application.
	 */
	public void appInstalled(AppID app);

	/**
	 * Call back method that signals an removed application.
	 * 
	 * @param app
	 *            The {@link #AppID} object of the removed application.
	 */
	public void appRemoved(AppID app);
}
