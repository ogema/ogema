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
package org.ogema.model.connections;

import org.ogema.model.prototypes.Connection;
import org.ogema.model.sensors.FlowSensor;
import org.ogema.model.sensors.PowerSensor;
import org.ogema.model.sensors.TemperatureSensor;
import org.ogema.model.sensors.VolumeAccumulatedSensor;

/**
 * Connection characterized by a fluid (gas, water, fuel) flowing from a 
 * device to another.
 */
public interface FluidConnection extends Connection {
	/** 
	 * Flow sensor measuring the flow of the fluid from "input" to "output" (negative values 
	 * indicate flow from "out" to "in"). 
	 */
	FlowSensor flowSensor();

	/**
	 * A generic power sensor. For fuel connections this measures the energy content of the fuel.
	 */
	PowerSensor powerSensor();

	/**
	 * Sensor for the temperature of the fluid, including target settings and forecasts.
	 */
	TemperatureSensor temperatureSensor();

	/**
	 * Sensor for accumulated flow (=Volume)
	 */
	VolumeAccumulatedSensor volumeSensor();
}
