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

import org.ogema.model.actors.Actor;
import org.ogema.model.sensors.GenericBinarySensor;
import org.ogema.model.sensors.GenericMultiSensor;
import org.ogema.model.sensors.TouchSensor;
import org.ogema.model.prototypes.PhysicalElement;

/** 
 * A manual switch. Can contain a binary switch and/or a touch sensor and/or a multi switch (e.g. a dimmer). 
 */
public interface ManualSwitch extends PhysicalElement {

	/** A touch sensor */
	TouchSensor touchSensor();

	/** A binary sensor (switch) */
	GenericBinarySensor trigger();

	/** A dimmer, or any multi switch. */
	GenericMultiSensor dimmer();

	/** The actor controlled by the switch (usually as a reference). */
	Actor actor();
}
