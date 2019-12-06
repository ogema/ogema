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
package de.iwes.ogema.remote.rest.connector.model;

import java.util.ArrayList;
import java.util.List;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.osgi.framework.BundleContext;
import org.slf4j.LoggerFactory;

import de.iwes.ogema.remote.rest.connector.tasks.ConnectionTask;
import de.iwes.ogema.remote.rest.connector.tasks.PullTask;
import de.iwes.ogema.remote.rest.connector.tasks.PushParentInitTask;
import de.iwes.ogema.remote.rest.connector.tasks.PushTask;
import de.iwes.ogema.remote.rest.connector.tasks.TaskScheduler;

public class ConnectionConfiguration {

	private final PullTask pullTask;
	private final PushTask pushTask;
	private final Thread initThread;
	
	public ConnectionConfiguration(final RestConnection con, final ApplicationManager am, final BundleContext ctx, final TaskScheduler app) {
		this.pullTask = (isPull(con) ? new PullTask(con, am, ctx, app) : null);
		this.pushTask = (isPush(con) ? new PushTask(con, am, ctx, app) : null);
		if (con.createParentsLevelOnInit().isActive()) {
			final int level = con.createParentsLevelOnInit().getValue();
			if (hasParentsUpLevels(con.getParent(), level + 1)) {
				final PushParentInitTask initPushTask = new PushParentInitTask(con, am, ctx, level);
				this.initThread = new Thread(new Runnable() {
					
					@Override
					public void run() {
						try {
							// TODO retry on failed connection
							initPushTask.call();
						} catch (Exception e) {
							LoggerFactory.getLogger(getClass()).warn("Init task failed for connection {}, {}", con.remotePath().getValue(), con, e);
						}
					}
				}, "remote-rest-connector-init");
			}
			else
				this.initThread = null;
		} else {
			this.initThread = null;
		}
		
	}

	public List<ConnectionTask> getTasks() {
		List<ConnectionTask> tasks = new ArrayList<>();
		if (pullTask != null)
			tasks.add(pullTask);
		if (pushTask != null)
			tasks.add(pushTask);
		return tasks;
	}
	
	public void scheduleInitTask() {
		if (initThread != null)
			initThread.start();
	}
	
	public void close() {
		if (pullTask != null)
			pullTask.close();
		if (pushTask != null)
			pushTask.close();
		if (initThread != null && initThread.isAlive()) {
			initThread.interrupt();
			try {
				initThread.join(2000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
			
	}
	
	private static boolean isPush(RestConnection con) {
		return con.pushConfig().isActive() || con.individualPushConfigs().isActive();
	}
	
	private static boolean isPull(RestConnection con) {
		return con.pullConfig().isActive() || con.individualPullConfigs().isActive();
	}
	
	private static boolean hasParentsUpLevels(Resource r, final int levels) {
		if (r == null)
			return false;
		for (int i=0; i < levels; i++) {
			try {
				r = r.getParent();
			} catch (SecurityException e) {
				return false; // we cannot transfer the resource in this case
			}
			if (r == null)
				return false;
		}
		return true;
	}
	
}
