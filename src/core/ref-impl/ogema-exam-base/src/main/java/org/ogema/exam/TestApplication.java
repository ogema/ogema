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
package org.ogema.exam;

import java.util.Dictionary;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.assertTrue;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * Test application that provides access to OGEMA framework services for testing.
 * 
 * 
 * @author jlapp
 */
public class TestApplication implements Application {

	private final CountDownLatch startLatch = new CountDownLatch(1);
	private ServiceRegistration<Application> registration;
	private ApplicationManager appman;

	public final void registerAndAwaitStart(BundleContext ctx) {
		registerAndAwaitStart(ctx, null);
	}

	public final void registerAndAwaitStart(BundleContext ctx, Dictionary<String, ?> serviceProps) {
		registration = ctx.registerService(Application.class, this, serviceProps);
		try {
			assertTrue("test app " + this + " failed to start", startLatch.await(10, TimeUnit.SECONDS));
		} catch (InterruptedException ex) {
			throw new AssertionError("test app " + this + " failed to start", ex);
		}
	}

	public final void unregister() {
		if (registration != null) {
			registration.unregister();
		}
	}

	@Override
	public final void start(ApplicationManager appManager) {
		this.appman = appManager;
		startLatch.countDown();
	}

	@Override
	public final void stop(AppStopReason reason) {
		doStop(reason);
	}

	public ApplicationManager getAppMan() {
		return appman;
	}

	/**
	 * Called at the end of {@link Application#start(org.ogema.core.application.ApplicationManager) },
	 * overwrite to customize test app behaviour.
	 */
	public void doStart() {
	}

	/**
	 * Called at the start of {@link Application#stop(org.ogema.core.application.Application.AppStopReason) },
	 * overwrite to customize test app behaviour.
	 * 
	 * @param reason
	 */
	public void doStop(AppStopReason reason) {
	}

}
