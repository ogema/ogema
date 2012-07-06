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
package org.ogema.app.coolcontrol;

import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.model.ranges.TemperatureRange;
import org.ogema.model.devices.whitegoods.CoolingDevice;

/**
 * Resource pattern for a cooling device controlled by this applications. This contains all data points used by the controller.
 * 
 * @author Timo Fischer, Fraunhofer IWES
 */
// Take note of the Generics parameter "CoolingDevice" here, this tells the framework that the basis model looked for is a cooling device, which is stored in the field "model". Most other fields in this class refer to this field.
public class CoolspacePattern extends ResourcePattern<CoolingDevice> {

	/**
	 * Device temperature reading.
	 */
	public final TemperatureResource temperature = model.temperatureSensor().reading();

	/**
	 * Control switch for the device: Default is required at least shared access.
	 */
	@Access(mode = AccessMode.SHARED)
	public BooleanResource stateControl = model.onOffSwitch().stateControl();

	private final TemperatureRange controlLimits = model.temperatureSensor().settings().controlLimits();

	/**
	 * Maximum temperature the device may attain.
	 */
	protected FloatResource m_maxTemp = controlLimits.upperLimit();

	/**
	 * Minimum cooling temperature.
	 */
	protected FloatResource m_minTemp = controlLimits.lowerLimit();

	/**
	 * Constructor for the access pattern. This constructor is invoked by the framework.
	 */
	public CoolspacePattern(Resource device) {
		super(device);
	}

	/**
	 * Initializes the optional fields if they did not exist, yet.
	 */
	public void init() {
	}

	/**
	 * Does this pattern represent the device passed?
	 */
	public boolean isDevice(Resource resource) {
		return model.equalsLocation(resource);
	}
}
