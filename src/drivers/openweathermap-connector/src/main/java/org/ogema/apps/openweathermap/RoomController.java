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

	private ApplicationManager appMan;
	private RoomRad device;
	private long SCHEDULE_UPDATE_TIME = 10 * 60 * 1000;
	private TimerTask task;

	public RoomController(ApplicationManager appMan, RoomRad rad) {

		this.appMan = appMan;
		this.device = rad;
	}

	public void start() {

		

		if (device.irradSensor.reading() != null
				|| device.tempSens.reading() != null || device.country != null
				|| device.city != null) {

			final ResourceUtil util = new ResourceUtil(appMan,
					device.tempSens.reading(), device.irradSensor.reading());

			TimerTask task = new TimerTask() {

				@Override
				public void run() {
					
					appMan.getLogger().info(
							"update weather info for location " + device.model.getName()
									+ " next update in " + SCHEDULE_UPDATE_TIME + "ms");
					
					util.update(device.city.getValue(),
							device.country.getValue());
				}
			};

			try {
				SCHEDULE_UPDATE_TIME = Long
						.parseLong(System
								.getProperty("org.ogema.drivers.openweathermap.getWeatherInfoRepeatTime"));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				appMan.getLogger().error(e.getMessage());
			}

			Timer t = new Timer();
			t.schedule(task, 0, SCHEDULE_UPDATE_TIME);

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
