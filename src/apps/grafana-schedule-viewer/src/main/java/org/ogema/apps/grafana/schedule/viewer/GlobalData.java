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
package org.ogema.apps.grafana.schedule.viewer;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import org.ogema.core.model.Resource;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceDemandListener;

// no strong references must be held to this
class GlobalData {
	
	private final ResourceAccess ra;
	
	/*
	 * Parent resource types
	 */
	final Set<String> programTypes = new ConcurrentSkipListSet<>();
	final Set<String> forecastTypes = new ConcurrentSkipListSet<>();
	final Set<String> historicalTypes = new ConcurrentSkipListSet<>();
	final Set<String> otherTypes = new ConcurrentSkipListSet<>();
	
	// resource type -> resources paths; concurrent
	// we avoid storing the resources themselves, since different users may have different access rights
	final Map<Class<? extends Resource>, Collection<String>> resourcesByParentClass = new ConcurrentHashMap<>();
	final Set<String> resourcesByScheduleClass = new ConcurrentSkipListSet<>(); 
	
	private final GlobalListener listener = new GlobalListener(resourcesByParentClass, resourcesByScheduleClass, programTypes, forecastTypes, historicalTypes, otherTypes);
	
	private static class GlobalListener implements ResourceDemandListener<Schedule> {
		
		private final Map<Class<? extends Resource>, Collection<String>> resourcesByParentClass;
		private final Set<String> resourcesByScheduleClass;
		private final Set<String> programTypes;
		private final Set<String> forecastTypes;
		private final Set<String> otherTypes;
		private final Set<String> historicalTypes;
		
		GlobalListener(Map<Class<? extends Resource>, Collection<String>> resources, Set<String> resourcesByScheduleClass,
				final Set<String> programTypes,
				final Set<String> forecastTypes,
				final Set<String> historicalTypes,
				final Set<String> otherTypes) {
			GrafanaScheduleViewer.logger.info("Starting global resource demand listener");
			this.resourcesByParentClass = resources;
			this.resourcesByScheduleClass = resourcesByScheduleClass;
			this.programTypes = programTypes;
			this.forecastTypes = forecastTypes;
			this.otherTypes = otherTypes;
			this.historicalTypes = historicalTypes;
		}

		@Override
		public void resourceAvailable(Schedule resource) {
			// getParent() may throw SecurityException; but we needn't care here
			final Class<? extends Resource> type = (Class<? extends Resource>) resource.getParent().getResourceType();
			final String typeName = type.getName();
			if (!resourcesByParentClass.containsKey(type)) {
				resourcesByParentClass.put(type, new ConcurrentSkipListSet<String>());
			}
			resourcesByParentClass.get(type).add(resource.getPath());
			resourcesByScheduleClass.add(resource.getPath());
			switch (resource.getName()) {
			case "program":
				programTypes.add(typeName);
				break;
			case "forecast":
				forecastTypes.add(typeName);
				break;
			case "historicalData":
				historicalTypes.add(typeName);
				break;
			default:
				otherTypes.add(typeName);
			}
			
		}

		@Override
		public void resourceUnavailable(Schedule resource) {
			final Class<? extends Resource> type = (Class<? extends Resource>) resource.getParent().getResourceType();
			final String typeName = type.getName();
			resourcesByParentClass.get(type).remove(resource.getPath());
			resourcesByScheduleClass.remove(resource.getPath());
			if (resourcesByParentClass.get(type).isEmpty()) {
				resourcesByParentClass.remove(type);
				programTypes.remove(typeName);
				forecastTypes.remove(typeName);
				historicalTypes.remove(typeName);
				otherTypes.remove(typeName);
			}
		}
	}
	
	GlobalData(ResourceAccess ra) {
		this.ra = ra;
		ra.addResourceDemand(Schedule.class, listener);
	}
	
	void close() {
		ra.removeResourceDemand(Schedule.class, listener);
		GrafanaScheduleViewer.logger.info("Global resource demand listener unregistered");
	}
	
	@Override
	protected void finalize() throws Throwable {
		close();
	}

}
