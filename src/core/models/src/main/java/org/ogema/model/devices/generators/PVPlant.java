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
package org.ogema.model.devices.generators;

import org.ogema.core.model.ResourceList;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.units.AngleResource;
import org.ogema.core.model.units.AreaResource;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.model.sensors.GeographicDirectionSensor;
import org.ogema.model.sensors.SolarIrradiationSensor;

/**
 * Resource type for a PV plant. As a complete physical device, this is intended
 * to be used as a top-level resource.
 */
public interface PVPlant extends ElectricityGenerator {

	/**
	 * Total area of the panels.
	 */
	AreaResource panelArea();

	/**
	 * Inclination of the modules in degrees. <br>
	 * 0 : horizontal <br>
	 * 90 : vertical.
	 */
	AngleResource inclination();

	/**
	 * Azimuthal orientation of the modules, from 0° to 360°. <br>
	 */
	GeographicDirectionSensor azimuth();

	/**
	 * Change in panel output power per temperature unit. Unit = 1/K. <br>
	 * Note that this quantity is normally given in %/K instead of 1/K (factor 100 difference!). <br>
	 * In practice, this value is temperature dependent. TODO
	 * */
	FloatResource temperatureCoefficient();

	/**
	 * Nominal Operating Cell Temperature (TNOCT), <br>
	 * defined as the mean panel cell temperature for an open-rack mounted module in idle operation @ solar irradiance
	 * of 800 W/m2 on the panel surface, ambient temperature of 20°C, wind speed of 1 m/s.
	 */
	TemperatureResource nominalOperatingCellTemperature();

	/**
	 * Solar panel installation type <br>
	 * 1: roof mount <br>
	 * 2: free standing <br>
	 * 3: building integrated PV <br>
	 * 10: flush mount roof installation (panels have the same orientation as the roof, panels close to roof) <br>
	 * 11: open-rack roof installation <br>
	 * 20: open-rack free standing <br>
	 * 21: pole-mounted free standing
	 */
	IntegerResource installationType();

	/**
	 * 1: fixed installation, no tracking <br>
	 * 2: single-axis tracking: horizontal <br>
	 * 2: single-axis tracking: vertical <br>
	 * 3: dual-axis tracking
	 * */
	IntegerResource tracking();
	
	/**
	 * Solar irradiation sensors relevant to this plant.
	 * @return
	 */
	ResourceList<SolarIrradiationSensor> irradiationSensors();
	
}
