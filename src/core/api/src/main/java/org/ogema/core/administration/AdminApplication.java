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
package org.ogema.core.administration;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.ogema.core.application.AppID;
import org.ogema.core.application.ApplicationManager;
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
	 * Get the application manager for this app.
	 * @return
	 */
	ApplicationManager getAppManager();

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
