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
package org.ogema.core.application;

/**
 * May be installed in parallel to {@link Application} for applications requiring a callback when newly installed or
 * being uninstalled
 */
public interface InstallationStatusListener {
	/**
	 * Called by framework when application is installed before any callback is performed to the application.
	 */
	public void applicationInstalled();

	/**
	 * Called by the framework before application is uninstalled. This method may be used to delete resources created by
	 * the application if the respective values are not set by any other application.
	 */
	public void applicationUninstalled();
}
