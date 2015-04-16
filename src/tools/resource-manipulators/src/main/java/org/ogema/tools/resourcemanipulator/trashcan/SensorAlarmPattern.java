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
package org.ogema.tools.resourcemanipulator.trashcan;

import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.model.actors.OnOffSwitch;
import org.ogema.model.sensors.GenericFloatSensor;

/**
 * Search pattern for complete configurations.
 * 
 * @author Timo Fischer, Fraunhofer IWES
 */
public class SensorAlarmPattern extends ResourceManipulatorPattern<SensorAlarmModel> {

	private final OnOffSwitch alarmSwitch = model.alarmSwitch();
	private final GenericFloatSensor sensor = model.sensor();

	/**
	 * Switch to trigger depending on the alarm settings.
	 */
	@Access(mode = AccessMode.EXCLUSIVE)
	public final BooleanResource trigger = alarmSwitch.stateControl();

	/**
	 * Current sensor reading.
	 */
	public final FloatResource reading = sensor.reading();

	/**
	 * Minimum sensor reading. If this does not exist, it cannot be violated.
	 */
	@Existence(required = CreateMode.OPTIONAL)
	public final FloatResource minReading = sensor.settings().alarmLimits().lowerLimit();

	/**
	 * Maximum sensor reading. If this does not exist, it cannot be violated.
	 */
	@Existence(required = CreateMode.OPTIONAL)
	public final FloatResource maxReading = sensor.settings().alarmLimits().upperLimit();

	/**
	 *  Default constructor required by OGEMA. Do not change.
	 */
	public SensorAlarmPattern(Resource res) {
		super(res);
	}

}
