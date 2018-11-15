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

import de.iwes.ogema.remote.rest.connector.tasks.ConnectionTask;
import de.iwes.ogema.remote.rest.connector.tasks.PullTask;
import de.iwes.ogema.remote.rest.connector.tasks.PushTask;
import de.iwes.ogema.remote.rest.connector.tasks.TaskScheduler;

public class ConnectionConfiguration {

	private final PullTask pullTask;
	private final PushTask pushTask;
	
	public ConnectionConfiguration(final RestConnection con, final ApplicationManager am, final TaskScheduler app) {
		this.pullTask = (isPull(con) ? new PullTask(con, am, app) : null);
		this.pushTask = (isPush(con) ? new PushTask(con, am, app) : null);
	}

	public List<ConnectionTask> getTasks() {
		List<ConnectionTask> tasks = new ArrayList<>();
		if (pullTask != null)
			tasks.add(pullTask);
		if (pushTask != null)
			tasks.add(pushTask);
		return tasks;
	}
	
	public void close() {
		if (pullTask != null)
			pullTask.close();
		if (pushTask != null)
			pushTask.close();
	}
	
	private static boolean isPush(RestConnection con) {
		return con.pushConfig().isActive() || con.individualPushConfigs().isActive();
	}
	
	private static boolean isPull(RestConnection con) {
		return con.pullConfig().isActive() || con.individualPullConfigs().isActive();
	}
	
}
