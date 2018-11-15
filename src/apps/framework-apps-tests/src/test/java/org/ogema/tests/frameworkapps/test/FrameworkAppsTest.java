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
package org.ogema.tests.frameworkapps.test;

import org.junit.Assert;
import org.junit.Test;
import org.ogema.core.administration.AdminApplication;
import org.ogema.core.administration.AdministrationManager;
import org.ogema.core.application.Application;
import org.ogema.core.application.Application.AppStopReason;
import org.ogema.tests.frameworkapps.testbase.AppsTestBase;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;

@ExamReactorStrategy(PerMethod.class)
public class FrameworkAppsTest extends AppsTestBase {
	
	public FrameworkAppsTest() {
		super(true);
	}
	
	@Test(timeout=60000)
	public void allFrameworkAppsStart() throws InterruptedException {
		// allow the other apps to execute their start method
		Thread.sleep(2000);
		final AdministrationManager admin = getApplicationManager().getAdministrationManager();
		for (String symbName : FRAMEWORK_APPS.keySet()) {
			final AdminApplication app = getAppWithRetry(symbName, admin, 5000);
			Assert.assertNotNull("App not found: " + symbName, app);
			final Application startClass = app.getID().getApplication();
			startClass.stop(AppStopReason.APP_STOP);
			startClass.start(getApplicationManager());
		}
		System.out.println(" All apps started succesfully!");
	}

}
