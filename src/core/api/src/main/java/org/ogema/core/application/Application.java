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
