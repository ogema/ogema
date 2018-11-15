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
