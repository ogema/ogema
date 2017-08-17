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
