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
/**
 * This package contains interface that maintain the installation of OGEMA applications.
 * The InstallationManager owns the registration of different ApplicationSource implementation
 * and could provide a default implementation of this interface. Since the installation of an
 * application process can take more than one steps it manages this process and the states
 * of the application during the installation process. The status information of the application
 * that should be installed are covered by InstallableApplication.
 * 
 */
package org.ogema.core.installationmanager;

