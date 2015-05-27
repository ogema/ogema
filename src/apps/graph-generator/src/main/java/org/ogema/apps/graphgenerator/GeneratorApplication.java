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
import org.ogema.core.resourcemanager.ResourceAccess;

/**
 * Application that writes out the current state of the resource graph on
 * request.
 *
 * @author Timo Fischer, Fraunhofer IWES
 */
@Component(specVersion = "1.1", immediate = true)
@Service(Application.class)
public class GeneratorApplication implements Application {

	private OgemaLogger logger;
	private ApplicationManager appMan;
	private ResourceAccess resAcc;
	private GraphGenServlet servlet;

	@Override
	public void start(ApplicationManager appManager) {
		this.appMan = appManager;
		this.logger = appManager.getLogger();
		this.resAcc = appManager.getResourceAccess();
		this.servlet = new GraphGenServlet(resAcc, appMan.getSerializationManager(), logger);
		logger.debug("{} started", getClass().getName());

		appManager.getWebAccessManager().registerWebResource("/ogema/graphgenerator",
				"org/ogema/app/graphgenerator/gui");
		appManager.getWebAccessManager().registerWebResource("/apps/ogema/graphgenerator", servlet);
	}

	@Override
	public void stop(AppStopReason reason) {
		appMan.getWebAccessManager().unregisterWebResource("/ogema/graphwizzgenerator");
		appMan.getWebAccessManager().unregisterWebResource("/apps/ogema/graphwizzgenerator");

		logger.debug("{} stopped", getClass().getName());
	}
}
