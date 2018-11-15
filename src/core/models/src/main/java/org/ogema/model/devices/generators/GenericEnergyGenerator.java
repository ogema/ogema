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
import org.ogema.core.model.simple.TimeResource;
import org.ogema.model.actors.MultiSwitch;
import org.ogema.model.actors.OnOffSwitch;
import org.ogema.model.prototypes.PhysicalElement;
import org.ogema.model.ranges.PowerRange;

/**
 * Base class for an energy generating device or information about a device's
 * energy generation part. Energy generating devices should extend this model.
 * This model itself should only be used if no suitable more specialized model
 * exists. Note that the sensor for the generated power is in the derived models'
 * connections.
 */
public interface GenericEnergyGenerator extends PhysicalElement {

	/** 
	 * On/off Switch 
	 */
	OnOffSwitch onOffSwitch();

	/** 
	 * Switch rating for the power production, in case the device allows 
	 * regulating the power production. The switch setting is to be
	 * understood as share of the maximum production power.
	 */
	MultiSwitch setting();

	/**
	 * Range of rated power generation.
	 */
	PowerRange ratedPower();

	/** 
	 * Conversion efficiency: output energy divided by input energy 
	 */
	FloatResource efficiency();

	/**
	 * start up time from cold start of energy generating unit to 90% of rated power
	 */
	TimeResource startupTimeCold();

	/**
	 * start up time from warm start (output power just reduced to zero) to 90% of rated power
	 */
	TimeResource startupTimeWarm();
}
