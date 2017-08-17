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
package de.iwes.ogema.remote.rest.connector.tasks;

import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.ResourceValueListener;

public class PullListener implements ResourceValueListener<Resource> {
	
	private final PullTask pullTask;
	private final TaskScheduler trigger;
	
	public PullListener(PullTask pullTask, TaskScheduler trigger) {
		this.pullTask = pullTask;
		this.trigger = trigger;
		pullTask.con.triggerPull().addValueListener(this);
	}
	
	void close() {
		try {
			pullTask.con.triggerPull().removeValueListener(this);
		} catch (Exception e) { 
			e.printStackTrace();
		}
	}

	@Override
	public void resourceChanged(Resource resource) {
		if (resource.equalsLocation(pullTask.con.triggerPull())) {
			if (!pullTask.con.triggerPull().getValue())
				return;
			else
				pullTask.con.triggerPull().setValue(false);
		}
		pullTask.triggerImmediately();
		trigger.reschedule(pullTask);
	}
	
}
