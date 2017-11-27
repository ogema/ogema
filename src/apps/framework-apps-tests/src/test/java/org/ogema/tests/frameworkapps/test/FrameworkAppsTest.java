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
