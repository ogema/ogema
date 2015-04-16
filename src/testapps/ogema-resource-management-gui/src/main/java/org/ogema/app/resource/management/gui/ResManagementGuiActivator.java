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
package org.ogema.app.resource.management.gui;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.osgi.framework.Bundle;
import org.osgi.service.component.ComponentContext;

@Component(specVersion = "1.1", immediate = true)
@Service(Application.class)
public class ResManagementGuiActivator implements Application {

	protected OgemaLogger logger;
	protected ApplicationManager appManager;
	private static Bundle bundle = null;

	protected static ApplicationManager staticAppMan = null;

	public static ApplicationManager getAppManager() {
		return staticAppMan;
	}

	@Override
	public void start(ApplicationManager appManager) {
		this.appManager = appManager;
		ResManagementGuiActivator.staticAppMan = appManager;
		logger = appManager.getLogger();
		logger.debug("{} started", getClass().getName());
	}

	@Override
	public void stop(AppStopReason reason) {
		logger.debug("{} stopped", getClass().getName());
	}

	@Activate
	protected void activate(ComponentContext componentContext) {
		ResManagementGuiActivator.bundle = componentContext.getBundleContext().getBundle();
	}

	public static Bundle getBundle() {
		return ResManagementGuiActivator.bundle;
	}
}
