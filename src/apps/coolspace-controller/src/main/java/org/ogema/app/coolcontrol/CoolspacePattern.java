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
