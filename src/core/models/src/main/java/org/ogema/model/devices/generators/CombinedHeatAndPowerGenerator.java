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
