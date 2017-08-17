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
package org.ogema.apps.openweathermap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.felix.scr.annotations.Component;
//import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.ogema.apps.openweathermap.resources.EnvironmentCreater;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.core.resourcemanager.pattern.PatternListener;
import org.ogema.core.resourcemanager.pattern.ResourcePatternAccess;

//import org.osgi.service.cm.ConfigurationAdmin;

/**
 * 
 * Application main class. Application connect to the openweathermap services (www.openweathermap.com) and store weather
 * information (temperature,cloudiness, humidity, rain) into OGEMA resources. Also calculate solar irradiation.
 * 
 * @author brequardt
 */
@Component(specVersion = "1.2", immediate = true)
@Service({ Application.class, OpenWeatherMapApplicationI.class })
public class OpenWeatherMapApplication implements OpenWeatherMapApplicationI {

	/**
	 * System property ({@value} ) holding the interval in ms at which weather data will be retrieved.
	 */
	public static final String UPDATE_INTERVAL = "org.ogema.drivers.openweathermap.getWeatherInfoRepeatTime";

	/**
	 * Default value ({@value} ) for {@link #UPDATE_INTERVAL}.
	 */
	public static final long UPDATE_INTERVAL_DEFAULT = 10 * 60 * 1000L;

	public OgemaLogger logger;
	protected ApplicationManager appMan;
	protected ResourceManagement resMan;
	// @Reference
	// private ConfigurationAdmin configurationAdmin;
	public static OpenWeatherMapApplication instance;
	EnvironmentCreater envCreater;
	private List<RoomController> roomConntrollers = new ArrayList<>();
	private ResourcePatternAccess advAcc;

	@Override
	public void start(ApplicationManager appManager) {

		instance = this;
		this.appMan = appManager;
		this.logger = appManager.getLogger();
		this.resMan = appManager.getResourceManagement();
		envCreater = EnvironmentCreater.getInstance();
		envCreater.init(appManager);
		String stdCity = System.getProperty("org.ogema.drivers.openweathermap.stdCity");
		String stdCountry = System.getProperty("org.ogema.drivers.openweathermap.stdCountry");
		if ((stdCity != null) && (stdCountry != null)) {
			envCreater.createResource("OpenWeatherMapData", stdCity, stdCountry);
		}
		advAcc = appManager.getResourcePatternAccess();
		advAcc.addPatternDemand(RoomRad.class, roomListener, AccessPriority.PRIO_DEVICEGROUPMAN);
	}

	@Override
	public void stop(AppStopReason reason) {

		for (RoomController controller : roomConntrollers) {
			controller.stop();
		}
	}

	/**
	 * Create an environment OGEMA resource for saving weather information.
	 * 
	 * @param name
	 *            name of the environment
	 * @param city
	 *            name of the city
	 * @param country
	 *            name of country (shortcuts) example: de for germany
	 * @return OGEMA resource
	 */
	@Override
	public Resource createEnvironment(String name, String city, String country) {
		// TODO Auto-generated method stub
		return envCreater.createResource(name, city, country);
	}

	final PatternListener<RoomRad> roomListener = new PatternListener<RoomRad>() {

		@Override
		public void patternAvailable(RoomRad rad) {
			final RoomController newController = new RoomController(appMan, rad);
			roomConntrollers.add(newController);
			newController.start();
		}

		@Override
		public void patternUnavailable(RoomRad rad) {
			RoomController controller = null;
			for (RoomController existingController : roomConntrollers) {
				if (existingController.isControllingDevice(rad)) {
					controller = existingController;
					break;
				}
			}
			if (controller == null) {
				logger.warn("Got a resource unavailable callback for a RAD that has no controller.");
				return;
			}
			controller.stop();
			roomConntrollers.remove(controller);
		}
	};

	/**
	 * return environment parameters
	 * 
	 * @param name
	 *            name of the environment information
	 * @return return information inside a map
	 */
	@Override
	public Map<String, Object> getEnviromentParameter(String name) {
		// TODO Auto-generated method stub
		return envCreater.getParameters(name);
	}

}
