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
		appMan.getWebAccessManager().unregisterWebResource(MAINPAGE_ALIAS);
		appMan.getWebAccessManager().unregisterWebResource(SERVLET_ALIAS);

		logger.debug("{} stopped", getClass().getName());
	}
}
