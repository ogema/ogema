/**
 * Copyright 2011-2019 Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
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
package org.ogema.tools.scheduleimporter;

import java.util.HashMap;
import java.util.Map;

import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.pattern.PatternListener;
import org.ogema.tools.timeseriesimport.api.TimeseriesImport;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentServiceObjects;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(service=Application.class)
public class ScheduleCsvImporter implements Application, PatternListener<ConfigPattern> {

	@Reference(service=TimeseriesImport.class)
	private ComponentServiceObjects<TimeseriesImport> csvImporter;
	
	private BundleContext ctx;
	private ApplicationManager appMan;
	private final Map<ConfigPattern, Controller> controllers = new HashMap<>();
	
	protected void activate(BundleContext ctx) {
		this.ctx = ctx;
	}
	
	@Override
	public void start(ApplicationManager appMan) {
		this.appMan = appMan;
		appMan.getResourcePatternAccess().addPatternDemand(ConfigPattern.class, this, AccessPriority.PRIO_LOWEST);
	}

	@Override
	public void stop(AppStopReason reason) {
		final ApplicationManager appMan = this.appMan;
		this.appMan = null;
		if (appMan != null) {
			appMan.getResourcePatternAccess().removePatternDemand(ConfigPattern.class, this);
		}
		for (Controller c : controllers.values()) 
			c.close();
		this.controllers.clear();
	}

	@Override
	public void patternAvailable(ConfigPattern pattern) {
		final Controller controller = new Controller(pattern, appMan, ctx, csvImporter);
		if (controller.isActive()) {
			final Controller old = controllers.put(pattern, controller);
			if (old != null)
				old.close();
		}
	}

	@Override
	public void patternUnavailable(ConfigPattern pattern) {
		final Controller c = controllers.remove(pattern);
		if (c != null)
			c.close();
	}

	
}
