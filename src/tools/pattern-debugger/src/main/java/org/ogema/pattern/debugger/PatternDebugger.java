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
package org.ogema.pattern.debugger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceManagement;

@Component(specVersion = "1.2", immediate = true)
@Service(Application.class)
public class PatternDebugger implements Application {

	private OgemaLogger logger;
	private ApplicationManager am;
	private ResourceManagement rm;
	private ResourceAccess ra;

	@Override
	public void start(ApplicationManager appManager) {
		this.am = appManager;
		this.logger = appManager.getLogger();
		this.rm = appManager.getResourceManagement();
		this.ra = appManager.getResourceAccess();

		logger.debug("{} started", getClass().getName());
		String webResourcePackage = "org.ogema.pattern.debugger";
		webResourcePackage = webResourcePackage.replace(".", "/");

		appManager.getWebAccessManager().registerWebResourcePath("index.html", webResourcePackage);
		appManager.getWebAccessManager().registerWebResourcePath("servlet", new PatternDebuggerServlet(am));

	}

	@Override
	public void stop(AppStopReason reason) {
		am.getWebAccessManager().unregisterWebResourcePath("index.html");
		am.getWebAccessManager().unregisterWebResourcePath("servlet");
	}

}
