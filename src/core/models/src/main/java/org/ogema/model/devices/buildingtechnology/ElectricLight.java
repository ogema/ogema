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
