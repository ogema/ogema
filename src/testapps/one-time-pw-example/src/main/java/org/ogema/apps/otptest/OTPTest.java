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
package org.ogema.apps.otptest;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.model.locations.Building;

@Component(specVersion = "1.1", immediate = true)
@Service(Application.class)
public class OTPTest implements Application {

	@Override
	public void start(ApplicationManager appManager) {
		// Register some web resources
		try {
			appManager.getWebAccessManager().registerWebResource("/otp-example", "/web");
			appManager.getResourceAccess().getResources(Building.class);
			String userhome = System.getProperty("user.home");
		} catch (Throwable e) {
			System.out.println("Access to the property \"user.home\" is not permitted.");
		}
	}

	@Override
	public void stop(AppStopReason reason) {
	}

}
