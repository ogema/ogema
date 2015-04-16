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
package ogema.logging.app;

import java.util.ArrayList;
import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.application.Timer;
import org.ogema.core.application.TimerListener;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.SimpleResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceDemandListener;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.model.actors.Actor;
import org.ogema.model.sensors.ElectricEnergySensor;
import org.ogema.model.sensors.HumiditySensor;
import org.ogema.model.sensors.Sensor;
import org.ogema.model.sensors.StateOfChargeSensor;
import org.ogema.model.sensors.TemperatureSensor;

@Component(specVersion = "1.1", immediate = true)
@Service(Application.class)
public class LoggingApp implements Application {

	protected OgemaLogger logger;
	protected ApplicationManager am;
	protected ResourceManagement rm;
	protected ResourceAccess ra;

	private String webResourceBrowserPath;
	private String servletPath;

	@Override
	public void start(ApplicationManager appManager) {
		this.am = appManager;
		this.logger = appManager.getLogger();
		this.rm = appManager.getResourceManagement();
		this.ra = appManager.getResourceAccess();

		logger.debug("{} started", getClass().getName());
		String webResourcePackage = "ogema.logging.app";
		webResourcePackage = webResourcePackage.replace(".", "/");

		String appNameLowerCase = "LoggingApp";
		appNameLowerCase = appNameLowerCase.toLowerCase();

		//path to find the index.html /ogema/<this app name>/index.html
		webResourceBrowserPath = "/ogema/" + appNameLowerCase;
		//package/path to find the resources inside this application
		String webResourcePackagePath = webResourcePackage + "/gui";
		//path for the http servlet /apps/ogema/<this app name>
		servletPath = "/apps/ogema/" + appNameLowerCase;

		appManager.getWebAccessManager().registerWebResource(webResourceBrowserPath, webResourcePackagePath);
		appManager.getWebAccessManager().registerWebResource(servletPath, new LoggingAppServlet(am));

	}

	@Override
	public void stop(AppStopReason reason) {
		am.getWebAccessManager().unregisterWebResource(webResourceBrowserPath);
		am.getWebAccessManager().unregisterWebResource(servletPath);
	}

}
