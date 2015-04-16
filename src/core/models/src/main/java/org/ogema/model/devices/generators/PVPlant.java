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
package org.ogema.model.devices.generators;

import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.units.AngleResource;
import org.ogema.core.model.units.AreaResource;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.model.sensors.GeographicDirectionSensor;

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
	 * In practice, this value is temperature dependent. The dependence can be modeled using a
	 * {@link org.ogema.core.model.commondata.RelationCurve RelationCurve} decorator.
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
}
