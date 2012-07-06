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
