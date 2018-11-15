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
package org.ogema.apps.sample.scheduler;

import java.io.Closeable;
import java.io.IOException;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.resourcemanager.ResourceValueListener;

import osgi.enroute.scheduler.api.Scheduler;

@Component(service=Application.class)
public class SampleScheduler implements Application, ResourceValueListener<StringResource> {
	
	private final static String RESOURCE_NAME = "sampleSchedulerConfig";
	private Closeable task;
	private ApplicationManager appManager;
	
	@Reference
	Scheduler scheduler;

	@Override
	public void start(ApplicationManager appManager) {
		this.appManager = appManager; 
		final StringResource config = appManager.getResourceManagement().createResource(RESOURCE_NAME, StringResource.class);
		if (!config.isActive()) {
			config.setValue("* * * * * ?");
			config.activate(false);
		}
		appManager.getLogger().info("Sample scheduler started with Cron config {}", config.getValue());
		final String cron = config.getValue();
		startTask(cron);
		config.addValueListener(this);
	}
	
	@Override
	public void resourceChanged(StringResource resource) {
		final String cron = resource.getValue();
		appManager.getLogger().info("Cron task changed; new setting: {}", cron);
		closeTask();
		startTask(cron);
	}

	@Override
	public void stop(AppStopReason reason) {
		closeTask();
		try {
			appManager.getResourceAccess().getResource(RESOURCE_NAME).removeValueListener(this);
		} catch (Exception ignore) {}
		appManager = null;
	}
	
	private void closeTask() {
		if (task != null) {
			try {
				task.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		task = null;
	}
	
	private void startTask(final String cronExpression) {
		try {
			task = scheduler.schedule(
				onAppThread( () -> System.out.printf("Task executed on thread %s%n", Thread.currentThread().getName()), appManager), cronExpression);
		} catch (Exception e) {
			appManager.getLogger().error("Scheduling failed for cron expression {}",cronExpression,e);
		}
	}

	private static Scheduler.RunnableWithException onAppThread(final Scheduler.RunnableWithException r, final ApplicationManager appManager) {
        return () -> appManager.submitEvent(() -> {
            r.run();
            return null;
        });
	}

}
