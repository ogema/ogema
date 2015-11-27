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
package org.ogema.core.administration;

import java.util.List;

import org.ogema.core.application.AppID;
import org.osgi.framework.Bundle;

/**
 * Access to administration information and configuration for a single
 * application
 */
public interface AdminApplication {

	/**
	 * Gets the unique identifier of the application
	 */
	public AppID getID();

	/**
	 * Gets the bundle reference of the application
	 */
	public Bundle getBundleRef();

	/**
	 * Gets the resource demands registered by this application.
	 *
	 * @return the list of all of the application's registered resource demands.
	 */
	List<RegisteredResourceDemand> getResourceDemands();

	/**
	 * Gets the resource listeners added by this application.
	 *
	 * @return the list of all of the application's added resource listeners.
	 */
	List<RegisteredResourceListener> getResourceListeners();

	/**
	 * Gets the value listeners added by this application.
	 *
	 * @return the list of all of the application's added value listeners.
	 */
	List<RegisteredValueListener> getValueListeners();

	/**
	 * Gets the resource structure listeners added by one application or all
	 * applications.
	 *
	 * @return the list of all of the application's added resource structure
	 * listeners.
	 */
	List<RegisteredStructureListener> getStructureListeners();

	/**
	 * Gets the timers added by this application.
	 *
	 * @return the list of all of the application's added resource structure
	 * listeners.
	 */
	List<RegisteredTimer> getTimers();

	/**
	 * Gets the resource structure listeners added by this application.
	 *
	 * @return the list of all of the application's added resource structure
	 * listeners.
	 */
	List<RegisteredAccessModeRequest> getAccessModeRequests();

	/**
	 * Gets all loggers the application registered.
	 */
	public List<AdminLogger> getLoggers();

	/**
	 * return true if application is stopped via stop, other false
	 */
	public boolean isRunning();

	/*
	 * stop bundle, but do not uninstall
	 */
	//	public void stop();

	/*
	 * Stop and restart bundle
	 */
	//	public void start();

	/*
	 * stop and uninstall bundle
	 */
	//	public void remove();
}
