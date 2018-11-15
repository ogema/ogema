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

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.ogema.core.model.Resource;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.resourcemanager.ResourceAccess;

class SessionData {
	
	private final ResourceAccess ra;
	private final DataSupplier globalDataSupplier;
	private volatile SoftReference<GlobalData> softRef;
	
	SessionData(DataSupplier globalDataSupplier, ResourceAccess ra) {
		this.globalDataSupplier = globalDataSupplier;
		this.ra = ra;
		this.softRef = new SoftReference<GlobalData>(globalDataSupplier.getGlobalData());
	}

	Class<? extends Resource> getClass(final String name) {
		final GlobalData global = getGlobalData();
		for (Class<? extends Resource> type : global.resourcesByParentClass.keySet()) {
			if (type.getName().equals(name))
				return type;
		}
		try {
			return (Class<? extends Resource>) Class.forName(name);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}
	@SuppressWarnings("rawtypes")
	Map<String, Map> getPanels() {
		final GlobalData global = getGlobalData();
		final Map<String, Map> panels = new LinkedHashMap<>(global.resourcesByParentClass.size());
		for (Class<? extends Resource> key : global.resourcesByParentClass.keySet()) {
			final String type = key.getName();
			if (global.programTypes.contains(type)) {
				final String label = "Program: " + key.getSimpleName();
				panels.put(label, Collections.singletonMap(label, Schedule.class));
			}
			if (global.forecastTypes.contains(type)) {
				final String label = "Forecast: " + key.getSimpleName();
				panels.put(label, Collections.singletonMap(label, Schedule.class));
			}
			if (global.historicalTypes.contains(type)) {
				final String label = "Historical: " + key.getSimpleName();
				panels.put(label, Collections.singletonMap(label, Schedule.class));
			}
			if (global.otherTypes.contains(type)) {
				final String label = "Other: " + key.getSimpleName();
				panels.put(label, Collections.singletonMap(label, Schedule.class));
			}
		}
		return panels;
	}
	
	Map<String, Map<String, Class<? extends Resource>>> getRestrictions() {
		final GlobalData global = getGlobalData();
		final Map<String, Map<String, Class<? extends Resource>>> panels = new LinkedHashMap<>(global.resourcesByParentClass.size());
		for (Class<? extends Resource> key : global.resourcesByParentClass.keySet()) {
			final String type = key.getName();
			if (global.programTypes.contains(type)) {
				final String label = "Program: " + key.getSimpleName();
				panels.put(label, Collections.<String, Class<? extends Resource>> singletonMap(label, key));
			}
			if (global.forecastTypes.contains(type)) {
				final String label = "Forecast: " + key.getSimpleName();
				panels.put(label, Collections.<String, Class<? extends Resource>> singletonMap(label, key));
			}
			if (global.historicalTypes.contains(type)) {
				final String label = "Historical: " + key.getSimpleName();
				panels.put(label, Collections.<String, Class<? extends Resource>> singletonMap(label, key));
			}
			if (global.otherTypes.contains(type)) {
				final String label = "Other: " + key.getSimpleName();
				panels.put(label, Collections.<String, Class<? extends Resource>> singletonMap(label, key));
			}
		}
		return panels;
	}
	
	// FIXME adapt for schedules?
	List<? extends Resource> getResources(Class<? extends Resource> clazz) {
		final GlobalData global = getGlobalData();
		final Collection<String> list = global.resourcesByScheduleClass;
		if (list == null || list.isEmpty())
			return Collections.emptyList();
		final List<Resource> resources = new ArrayList<>(list.size());
		for (String path : list) {
			try {
				final Resource r = ra.getResource(path);
				if (r != null)
					resources.add(r);
			} catch (Exception expected) {} // typically a SecurityException
		}
		return resources;
	}
	
	private GlobalData getGlobalData() {
		GlobalData initial = softRef.get();
		if (initial != null)
			return initial;
		initial = globalDataSupplier.getGlobalData();
		softRef = new SoftReference<GlobalData>(initial);
		return initial;
	}
	
	static interface DataSupplier {
		
		GlobalData getGlobalData();
		
	}

}
