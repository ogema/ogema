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
package org.ogema.model.devices.sensoractordevices;

import org.ogema.core.model.ResourceList;
import org.ogema.model.actors.OnOffSwitch;
import org.ogema.model.connections.ElectricityConnection;
import org.ogema.model.devices.storage.ElectricityStorage;
import org.ogema.model.prototypes.PhysicalElement;
import org.ogema.model.ranges.PowerRange;
import org.ogema.model.sensors.Sensor;

/**
 * A single device containing one or multiple sensors. All sensors provided by sensor device shall be contained in the
 * sensor list as an instance of their actual sensor type. Models from {@link org.ogema.core.model.commonsensors} shall be used.
 */
public interface SensorDevice extends PhysicalElement {

	/**
	 * On/off switch of the device, in case it can be turned off.
	 */
	OnOffSwitch onOffSwitch();

	/**
	 * Electricity connection in case the device is connected to an electricity circuit.
	 */
	ElectricityConnection electricityConnection();

	/**
	 * Battery for a sensor device operating on batteries.
	 */
	ElectricityStorage electricityStorage();

	/**
	 * Rated electrical power for the device.
	 */
	PowerRange ratedPower();

	/**
	 * The sensors contained in the device. The sensors contained in this list
	 * do not need to be all of the same sensor type, so a SensorDevice can 
	 * for example contain a light sensor and a temperature sensor.
	 */
	ResourceList<Sensor> sensors();
}
