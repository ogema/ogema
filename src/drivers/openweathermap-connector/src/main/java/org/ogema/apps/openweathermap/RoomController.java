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

import java.util.Timer;
import java.util.TimerTask;

import org.ogema.core.application.ApplicationManager;

/**
 * 
 * Control the environment resource (OGEMA room resource/outside room).
 * 
 * @author brequardt
 */
public class RoomController {

	private final ApplicationManager appMan;
	private final RoomRad device;
	private final long scheduleUpdateTime;
	private TimerTask task;

	public RoomController(ApplicationManager appMan, RoomRad rad) {
		scheduleUpdateTime = Long.getLong(OpenWeatherMapApplication.UPDATE_INTERVAL,
				OpenWeatherMapApplication.UPDATE_INTERVAL_DEFAULT);
		this.appMan = appMan;
		this.device = rad;
	}

	public void start() {

		if (device.irradSensor.reading() != null || device.tempSens.reading() != null || device.country != null
				|| device.city != null) {

			final ResourceUtil util = new ResourceUtil(appMan, device);

			task = new TimerTask() {

				@Override
				public void run() {

					appMan.getLogger().info(
							"update weather info for location " + device.model.getName() + " next update in "
									+ scheduleUpdateTime + "ms");

					util.update(device.city.getValue(), device.country.getValue());
				}
			};

			Timer t = new Timer();
			t.schedule(task, 0, scheduleUpdateTime);

		}
	}

	public boolean isControllingDevice(RoomRad rad) {
		return device.model.equalsLocation(rad.model);
	}

	public void stop() {
		if (task != null) {
			task.cancel();
		}
	}

}
