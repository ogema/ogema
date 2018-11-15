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
package org.ogema.model.metering;

import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.model.units.EnergyResource;
import org.ogema.core.model.units.PowerResource;
import org.ogema.model.connections.ElectricityConnection;
import org.ogema.model.devices.connectiondevices.ElectricityConnectionBox;
import org.ogema.model.smartgriddata.ElectricityPrice;

/**
 * Meter for electricity (electric power and possibly accumulated energy).
 */
public interface ElectricityMeter extends GenericMeter {

	/**
	 * Electricity connection
	 */
	@Override
	ElectricityConnection connection();

	/**
	 * Power readings of the meter. The history of past values should be added
	 * in the definition {@link FloatResource#historicalData() historicalData} field of the reading.
	 */
	PowerResource powerReading();

	/**
	 * Energy readings of the meter. The history of past values should be added
	 * in the definition {@link FloatResource#historicalData() historicalData} field of the reading.
	 */
	EnergyResource energyReading();

	/**
	 * Time at which the energy reading was last reset (i.e. time of reference at
	 * which {@link #energyReading()} started at zero. Past values (past resets)
	 * should be added in the {@link FloatResource#historicalData() historicalData} 
	 * subresource of this field.
	 */
	TimeResource resetTime();

	/**
	 * Type of meter:<br>
	 * 1: two-way meter without nonreturn<br>
	 * 2: consumption meter<br>
	 * 3: generation meter<br>
	 * 4: capacitive reactive power<br>
	 * 5: generation, inductive reactive power<br>
	 * greater/equal 10.000: custom values
	 */
	@Override
	IntegerResource type();

	@Override
	ElectricityPrice price();

	@Override
	ElectricityConnectionBox distributionBox();
}
