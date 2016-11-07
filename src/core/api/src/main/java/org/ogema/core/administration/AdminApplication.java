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

import javax.servlet.http.HttpServletRequest;

import org.ogema.core.application.AppID;
import org.ogema.core.security.WebAccessManager;
import org.osgi.framework.Bundle;

/**
 * Access to administration information and configuration for a single
 * application
 */
public interface AdminApplication {

	/**
	 * Gets the unique identifier of the application
	 */
	AppID getID();

	/**
	 * Gets the bundle reference of the application
	 */
	Bundle getBundleRef();

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
	 * Gets the pattern listeners registered by this application.
	 * 
	 * @return registered pattern listeners.
	 */
	List<RegisteredPatternListener> getPatternListeners();

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
	List<AdminLogger> getLoggers();
	
	/**
	 * Get the WebAccessManager associated to this application. It allows, for instance,
	 * to access the registered servlets and web resources. 
	 */
	WebAccessManager getWebAccess();
	
	/**
	 * Is the user identified by the request allowed to access web resources of this app?
	 * @param req
	 */
	boolean isWebAccessAllowed(HttpServletRequest req);

	/**
	 * return true if application is stopped via stop, other false
	 */
	boolean isRunning();

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
