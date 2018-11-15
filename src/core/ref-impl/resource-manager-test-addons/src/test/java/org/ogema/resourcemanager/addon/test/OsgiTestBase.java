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
package org.ogema.resourcemanager.addon.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.core.resourcemanager.ResourceStructureListener;
import org.ogema.exam.OsgiAppTestBase;
import org.osgi.framework.ServiceRegistration;

/**
 *
 * @author jlapp
 */
public class OsgiTestBase extends OsgiAppTestBase {

	protected ResourceAccess resAcc;
	protected ResourceManagement resMan;
	protected final AtomicInteger counter = new AtomicInteger(1);

	public OsgiTestBase() {
		super(true);
	}

	@Override
	public void doStart(ApplicationManager appMan) {
		super.doStart(appMan);
		resAcc = appMan.getResourceAccess();
		resMan = appMan.getResourceManagement();
	}
	
	@Override
	public void doStop() {
		resAcc = null;
		resMan = null;
	}
	
	protected final List<TestApp> apps = new ArrayList<>();
	
	public void startApps(int nr) throws InterruptedException {
		CountDownLatch startLatch = new CountDownLatch(nr);
		for (int i=0; i<nr; i++) {
			TestApp app= new TestApp(startLatch);
			apps.add(app);
			app.thisReg = ctx.registerService(Application.class, app, null);
		}
		Assert.assertTrue("Apps not started", startLatch.await(30, TimeUnit.SECONDS));
	}
	
	public void shutdownApps() throws InterruptedException {
		for (TestApp app: apps) {
			app.cleanUp();
			app.thisReg.unregister();
			app.thisReg = null;
		}
		Thread.sleep(1000);
		for (TestApp app: apps) {
			app.assertStopped(5, TimeUnit.SECONDS);
		}
	}
	
	protected class TestApp implements Application {
		
		private final CountDownLatch startLatch;
		private final CountDownLatch stopLatch = new CountDownLatch(1);
		private volatile ApplicationManager am; 
		private ServiceRegistration<Application> thisReg;
		private final Map<String,ResourceStructureListener> structureListeners  = new HashMap<>();
		private final int id;
		
		public TestApp(CountDownLatch startLatch) {
			this.startLatch = startLatch;
			this.id = counter.getAndIncrement();
		}

		@Override
		public void start(ApplicationManager appManager) {
			Assert.assertNotNull(appManager);
			this.am = appManager;
			startLatch.countDown();
		}

		@Override
		public void stop(AppStopReason reason) {
			stopLatch.countDown();
			am = null;
		}
		
		public int getId() {
			return id;
		}
		
		public void assertStopped(long timeout, TimeUnit unit) throws InterruptedException {
			Assert.assertTrue("app not stopped",stopLatch.await(timeout, unit));
		}
		
		public ApplicationManager getAppManager() {
			return am;
		}
		
		public void addStructureListener(Resource res, ResourceStructureListener listener) {
			res.addStructureListener(listener);
			structureListeners.put(res.getPath(),listener);
		}
		
		private void cleanUp() {
			for (Map.Entry<String, ResourceStructureListener> entry: structureListeners.entrySet()) {
				try {
					am.getResourceAccess().getResource(entry.getKey()).removeStructureListener(entry.getValue());
				} catch (Exception e) {}
			}
			structureListeners.clear();
			
		}
		
	}

}
