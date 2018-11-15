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
