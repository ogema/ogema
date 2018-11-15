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

import org.ogema.model.actors.MultiSwitch;
import org.ogema.model.actors.OnOffSwitch;
import org.ogema.model.prototypes.PhysicalElement;

/**
 * Electrical dimmer device.
 */
public interface ElectricDimmer extends PhysicalElement {

	/**
	 * To switch dimmer on and off (not the device controlled by the dimmer,
	 * which should have its own switch).
	 */
	OnOffSwitch onOffSwitch();

	/**
	 * The dimming switch of the dimmer. The value 1 of shall represent the
	 * undimmed situation (on), 0 the fully dimmed state (off).
	 */
	MultiSwitch setting();

	/**
	 * Device controlled by the dimmer.
	 */
	PhysicalElement device();
}
