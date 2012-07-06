/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ogema.driver.knxdriver.gui;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.driver.knxdriver.KNXdriverI;

@Service(Application.class)
@Component(specVersion = "1.2", immediate = true)
public class KnxJsGuiApp implements Application {

	@Reference
	private KNXdriverI knxDriver;
	private ApplicationManager appManager;

	@Override
	public void start(ApplicationManager appManager) {
		this.appManager = appManager;
		appManager.getWebAccessManager().registerWebResource("/ogema/knx", "org/ogema/knx/gui");
		appManager.getWebAccessManager().registerWebResource("/apps/ogema/knx", new KnxServlet(appManager, knxDriver));
	}

	@Override
	public void stop(AppStopReason reason) {
		appManager.getWebAccessManager().unregisterWebResource("/ogema/knx");
		appManager.getWebAccessManager().unregisterWebResource("/apps/ogema/knx");
	}
}
