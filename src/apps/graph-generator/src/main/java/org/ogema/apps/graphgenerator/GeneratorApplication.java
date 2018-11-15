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
package org.ogema.apps.graphgenerator;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;

/**
 * Application that writes out the current state of the resource graph on request.
 *
 * @author Timo Fischer, Fraunhofer IWES
 */
@Component(specVersion = "1.2", immediate = true)
@Service(Application.class)
public class GeneratorApplication implements Application {

	private static final String MAINPAGE_ALIAS = "/ogema/graphgenerator";
	private static final String SERVLET_ALIAS = "/apps/ogema/graphgenerator";
	private OgemaLogger logger;
	private ApplicationManager appMan;
	private GraphGenServlet servlet;

	@Override
	public void start(ApplicationManager appManager) {
		this.appMan = appManager;
		this.logger = appManager.getLogger();
		this.servlet = new GraphGenServlet(appManager);
		logger.debug("{} started", getClass().getName());

		appManager.getWebAccessManager().registerWebResource(MAINPAGE_ALIAS, "org/ogema/app/graphgenerator/gui");
		appManager.getWebAccessManager().registerWebResource(SERVLET_ALIAS, servlet);
	}

	@Override
	public void stop(AppStopReason reason) {
		if (appMan != null) {
			appMan.getWebAccessManager().unregisterWebResource(MAINPAGE_ALIAS);
			appMan.getWebAccessManager().unregisterWebResource(SERVLET_ALIAS);
		}
		if (logger != null)
			logger.debug("{} stopped", getClass().getName());
		appMan = null;
		logger = null;
		servlet = null;
	}
}
