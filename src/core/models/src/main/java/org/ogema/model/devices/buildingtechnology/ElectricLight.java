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
package org.ogema.model.devices.buildingtechnology;

import org.ogema.core.model.simple.FloatResource;
import org.ogema.model.actors.MultiSwitch;
import org.ogema.model.actors.OnOffSwitch;
import org.ogema.model.connections.ElectricityConnection;
import org.ogema.model.prototypes.PhysicalElement;
import org.ogema.model.ranges.PowerRange;
import org.ogema.model.sensors.LuminousFluxSensor;

/**
 * Electric light
 */
public interface ElectricLight extends PhysicalElement {

	/**
	 * Switch to control when the light is on or off
	 */
	OnOffSwitch onOffSwitch();

	/**
	 * Power setting for the light.
	 */
	MultiSwitch setting();

	/**
	 * A dimmer controlling the light, in case it is dimmable. The light's switches
	 * might reference to the dimmer's switches in case this exists.
	 */
	ElectricDimmer dimmer();

	/**
	 * Electrical connection
	 */
	ElectricityConnection electricityConnection();

	/**
	 * Rated power surge of the light.
	 */
	PowerRange ratedPower();

	/**
	 * The lighting state.
	 */
	LuminousFluxSensor luminousFluxSensor();

	/**
	 * Actual solid angle of radiation (not the planar angle). Values are in [0;
	 * 4pi].
	 */
	FloatResource radiationAngle();
}
