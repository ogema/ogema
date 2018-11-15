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
import org.ogema.tools.listener.util.TransitiveValueListener;
import org.ogema.tools.resource.util.ListenerUtils;

public class PushListener implements ResourceValueListener<Resource> {
	
	private final TransitiveValueListener<Resource> transitiveValueListener;
	private final PushTask pushTask;
	private final TaskScheduler trigger;
	private final Resource target;
	
	public PushListener(PushTask pushTask, TaskScheduler trigger) {
		this.pushTask = pushTask;
		this.trigger = trigger;
		this.target = pushTask.getTargetResource();
		final boolean needsRecursivePushTrigger = pushTask.isRecursivePushTrigger();
		
		if (needsRecursivePushTrigger) {
			transitiveValueListener = ListenerUtils.registerTransitiveValueListener(target, this, Resource.class, true);
		} else {
			transitiveValueListener = null;
			final boolean needsPushListener = pushTask.needsPushListener();
			if (needsPushListener) 
				target.addValueListener(this, true);
		}
		pushTask.con.triggerPush().addValueListener(this);
	}
	
	void close() {
		try {
			if (transitiveValueListener != null)
				transitiveValueListener.destroy();
			target.removeValueListener(this);
			pushTask.con.triggerPush().removeValueListener(this);
		} catch (Exception e) { 
			e.printStackTrace();
		}
	}

	@Override
	public void resourceChanged(Resource resource) {
		if (resource.equalsLocation(pushTask.con.triggerPush())) {
			if (!pushTask.con.triggerPush().getValue())
				return;
			else
				pushTask.con.triggerPush().setValue(false);
		}
		pushTask.triggerImmediately();
		trigger.reschedule(pushTask);
	}
	
}
