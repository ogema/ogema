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
import org.ogema.model.actors.OnOffSwitch;
import org.ogema.model.actors.MultiSwitch;
import org.ogema.model.connections.FluidConnection;
import org.ogema.model.devices.storage.ThermalStorage;
import org.ogema.model.prototypes.PhysicalElement;

/**
 * Combined heat and power generator. This resource type contains the heat
 * and electricity generating units as sub-resources. The control settings
 * shared between both generating units are defined on this level of the device.
 */
public interface CombinedHeatAndPowerGenerator extends PhysicalElement {
	/**
	 * On/off switch for the whole device.
	 */
	OnOffSwitch onOffSwitch();

	/**
	 * Combined power setting of heat and electricity as a ratio of the rated
	 * values. The settings are relative values referring to the rated powers
	 * of {@link #electricityGenerator() } and {@link #heatGenerator() }
	 */
	MultiSwitch setting();

	/** Electrical generator */
	ElectricityGenerator electricityGenerator();

	/** Heat generating unit, e.g. burner */
	HeatGenerator heatGenerator();

	/**
	 * Storage for domestic (drinking) water
	 */
	ThermalStorage domesticStorage();

	/**
	 * Storage for heating.
	 */
	ThermalStorage heatingStorage();

	/**
	 * Connection to fuel input
	 */
	FluidConnection fuelConnection();

	/**
	 * Ratio of electrical energy output to heat output at the nominal operating point. 
	 */
	FloatResource powerToHeatRatio();
}
