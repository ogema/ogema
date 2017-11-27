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
package org.ogema.tests.security.testbase.app.impl;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import org.junit.Assert;

/**
 * This class is not exported from the test bundle, so don't use it in another bundle.
 */
public class TestApp implements Application, BundleActivator {

	// internal resource path within jar
	public final static String WEBRESOURCE_PATH = "/securitytestapp/resource";
	private volatile ApplicationManager appMan;
	private volatile BundleContext ctx;
	private volatile CountDownLatch startLatch;
	private volatile CountDownLatch stopLatch;
	private volatile ServiceRegistration<Application> service;
	
	public static ApplicationManager getAppManager(final Application testApp, final long timeout, final TimeUnit unit) {
		try {
			final Field latchField = testApp.getClass().getDeclaredField("startLatch");
			latchField.setAccessible(true);
			final CountDownLatch latch = (CountDownLatch) latchField.get(testApp);
			Assert.assertTrue("App did not start",latch.await(timeout,unit));
			// note: testApp.getClass() is a TestApp.class, but possibly loaded from an unexpected class loader
			final Field field = testApp.getClass().getDeclaredField("appMan");
			field.setAccessible(true);
			return (ApplicationManager) field.get(testApp);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	{
		reset();
	}
	
	public void reset() {
		this.startLatch = new CountDownLatch(1);
		this.stopLatch = new CountDownLatch(1);
	}
	
	public boolean awaitStart(long timeout, TimeUnit unit) throws InterruptedException {
		return startLatch.await(timeout, unit);
	}
	
	public boolean awaitStop(long timeout, TimeUnit unit) throws InterruptedException {
		return stopLatch.await(timeout, unit);
	}
	
	public ApplicationManager getApplicationManager() {
		return appMan;
	}
	
	@Override
	public void start(BundleContext context) throws Exception {
		this.ctx = context;
		this.service = ctx.registerService(Application.class, this, null);
	}
	
	@Override
	public void stop(BundleContext context) throws Exception {
		this.ctx = null;
		final ServiceRegistration<Application> service = this.service;
		if (service != null) {
			try {
				service.unregister();
			} catch (Exception ignore) {}
		}
		this.service = null;
	}
	
	@Override
	public void start(ApplicationManager appManager) {
		this.appMan = appManager;
		startLatch.countDown();
	}
	
	@Override
	public void stop(AppStopReason reason) {
		stopLatch.countDown();
		this.appMan = null;
	}

}
