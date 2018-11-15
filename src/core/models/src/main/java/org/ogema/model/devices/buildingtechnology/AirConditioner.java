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

import org.ogema.core.model.simple.BooleanResource;
import org.ogema.model.connections.ThermalConnection;
import org.ogema.model.devices.generators.HeatPump;
import org.ogema.model.sensors.TemperatureSensor;

/**
 * Air conditioning operating on a thermalConnection. In principle, the device
 can operate in both directions on the thermalConnection. Usually, air conditioners
 have a target room/location whose temperature they are intended to control.
 This should be the {@link ThermalConnection#output() } of the thermalConnection, 
 whenever possible.
 */
public interface AirConditioner extends HeatPump {

	/**
	 * Determines whether air conditioner also reduces absolute humidity
	 */
	BooleanResource isDehumidifying();

	/**
	 * Temperature sensor of the target location this air conditioner is
	 * supposed to cool/heat. Will often be a reference to the
	 * {@link #thermalConnection()}s 
	 * {@link ThermalConnection#outputTemperature()}.
	 */
	TemperatureSensor temperatureSensor();

}
