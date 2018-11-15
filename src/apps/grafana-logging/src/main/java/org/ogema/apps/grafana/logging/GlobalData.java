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
package org.ogema.apps.grafana.logging;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceDemandListener;

// no strong references must be held to this
class GlobalData {
	
	private final ResourceAccess ra;
	// resource type -> resources paths; concurrent
	// we avoid storing the resources themselves, since different users may have different access rights
	final Map<Class<? extends Resource>, Collection<String>> resources = new ConcurrentHashMap<>();
	final Set<String> resourceTypes = new ConcurrentSkipListSet<>(); 
	private final GlobalListener listener = new GlobalListener(resources, resourceTypes);
	
	private static class GlobalListener implements ResourceDemandListener<SingleValueResource> {
		
		private final Map<Class<? extends Resource>, Collection<String>> resources;
		private final Set<String> resourceTypes;
		
		GlobalListener(Map<Class<? extends Resource>, Collection<String>> resources, final Set<String> resourceTypes) {
			GrafanaLogging.logger.info("Starting global resource demand listener");
			this.resources = resources;
			this.resourceTypes = resourceTypes;
		}

		@Override
		public void resourceAvailable(SingleValueResource resource) {
			@SuppressWarnings("unchecked")
			final Class<? extends SingleValueResource> type = (Class<? extends SingleValueResource>) resource.getResourceType();
			if (StringResource.class.isAssignableFrom(type))
				return;
			final String typeName = type.getName();
			if (!resources.containsKey(type)) {
				resources.put(type, new ConcurrentSkipListSet<String>());
//				logger.debug("New type added to logging panels: {}", typeName);
				resourceTypes.add(typeName);
			}
			resources.get(type).add(resource.getPath());
		}

		@Override
		public void resourceUnavailable(SingleValueResource resource) {
			@SuppressWarnings("unchecked")
			final Class<? extends SingleValueResource> type = (Class<? extends SingleValueResource>) resource.getResourceType();
			if (StringResource.class.isAssignableFrom(type))
				return;
			final String typeName = type.getName();
			if (!resources.containsKey(type))
				return;
			resources.get(type).remove(resource.getPath());
			if (resources.get(type).isEmpty()) {
				resources.remove(type);
				resourceTypes.remove(typeName);
			}
		}
	}
	
	GlobalData(ResourceAccess ra) {
		this.ra = ra;
		ra.addResourceDemand(SingleValueResource.class, listener);
	}
	
	void close() {
		ra.removeResourceDemand(SingleValueResource.class, listener);
		GrafanaLogging.logger.info("Global resource demand listener unregistered");
	}
	
	@Override
	protected void finalize() throws Throwable {
		close();
	}

}
