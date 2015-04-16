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
 * This interface must be implemented by all OGEMA applications and must be registered as an OSGi service.
 * 
 */
public interface Application {
	/**
	 * This method is called by the framework when the application OSGi service is recognized and when all framework
	 * services are available.
	 * 
	 * @param appManager
	 *            reference to the application manager of the framework which acts as single point of entry for all
	 *            further interaction of the application with the framework.
	 */
	public void start(ApplicationManager appManager);

	/** Reason for stopping an application */
	public enum AppStopReason {
		/** Application is stopped individually */
		APP_STOP,
		/** Entire framework is shutdown */
		FRAMEWORK_SHUTDOWN,
		/** Application is to be uninstalled */
		UNINSTALL
	}

	/**
	 * This method is called by the framework when the application shall be shutdown due to an request obtained via the
	 * user interface or due to a general framework shutdown.
	 */
	public void stop(AppStopReason reason);
}
