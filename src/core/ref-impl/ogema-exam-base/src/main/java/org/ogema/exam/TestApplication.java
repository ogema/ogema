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
