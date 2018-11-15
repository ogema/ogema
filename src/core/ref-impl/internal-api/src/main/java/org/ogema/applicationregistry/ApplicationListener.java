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
package org.ogema.applicationregistry;

import org.ogema.core.application.AppID;

/**
 * An implementation of this interface can be registered on {@link ApplicationRegistry} to get event call backs whwn an
 * application is installed or removed.
 *
 */
public interface ApplicationListener {
	/**
	 * Call back method that signals an installed application.
	 * 
	 * @param app
	 *            The {@link AppID} object of the installed application.
	 */
	public void appInstalled(AppID app);

	/**
	 * Call back method that signals an removed application.
	 * 
	 * @param app
	 *            The {@link AppID} object of the removed application.
	 */
	public void appRemoved(AppID app);
}
