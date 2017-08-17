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
