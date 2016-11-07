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
	protected final AtomicInteger counter = new AtomicInteger();

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
