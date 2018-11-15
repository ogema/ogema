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
