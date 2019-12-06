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
@Component(specVersion = "1.2")
@Service({ Application.class, OpenWeatherMapApplicationI.class })
public class OpenWeatherMapApplication implements OpenWeatherMapApplicationI {

	/**
	 * System property ({@value} ) holding the interval in ms at which weather data will be retrieved.
	 */
	public static final String UPDATE_INTERVAL = "org.ogema.drivers.openweathermap.getWeatherInfoRepeatTime";

	/**
	 * Default value ({@value} ) for {@link #UPDATE_INTERVAL}.
	 */
	public static final long UPDATE_INTERVAL_DEFAULT = 3 * 60 * 60 * 1000L;

	public OgemaLogger logger;
	protected ApplicationManager appMan;
	protected ResourceManagement resMan;
	// @Reference
	// private ConfigurationAdmin configurationAdmin;
	public static OpenWeatherMapApplication instance;
	EnvironmentCreater envCreater;
	private List<RoomController> roomControllers = new ArrayList<>();
	private ResourcePatternAccess advAcc;

	@Override
	public void start(ApplicationManager appManager) {

		instance = this;
		this.appMan = appManager;
		this.logger = appManager.getLogger();
		this.resMan = appManager.getResourceManagement();
		envCreater = new EnvironmentCreater(appManager);
		String stdCity = null;
		String stdCountry = null;
		try {
			stdCity = System.getProperty("org.ogema.drivers.openweathermap.stdCity");
			stdCountry = System.getProperty("org.ogema.drivers.openweathermap.stdCountry");
		} catch (SecurityException e) {
			logger.warn("Permission denied to access init properties",e);
		}
		if ((stdCity != null) && (stdCountry != null)) {
			envCreater.createResource("OpenWeatherMapData", stdCity, stdCountry);
		}
		advAcc = appManager.getResourcePatternAccess();
		advAcc.addPatternDemand(RoomRad.class, roomListener, AccessPriority.PRIO_DEVICEGROUPMAN);
	}

	@Override
	public void stop(AppStopReason reason) {

		for (RoomController controller : roomControllers) {
			controller.stop();
		}
		roomControllers.clear();
		advAcc.removePatternDemand(RoomRad.class, roomListener);
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
			roomControllers.add(newController);
			newController.start();
		}
		
		@Override
		public void patternUnavailable(RoomRad rad) {
			RoomController controller = null;
			for (RoomController existingController : roomControllers) {
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
			roomControllers.remove(controller);
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
