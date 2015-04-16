/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3
 * as published by the Free Software Foundation.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ogema.model.metering;

import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.model.units.EnergyResource;
import org.ogema.core.model.units.PowerResource;
import org.ogema.model.connections.ThermalConnection;
import org.ogema.model.devices.connectiondevices.HeatConnectionBox;
import org.ogema.model.smartgriddata.HeatPrice;

/**
 * Heat meter.
 */
public interface HeatMeter extends GenericMeter {

	@Override
	ThermalConnection connection();

	/**
	 * Power readings of the meter. The history of past values should be added
	 * in the definition {@link DefinitionForecast} of the reading.
	 */
	PowerResource powerReading();

	/**
	 * Energy readings of the meter. The history of past values should be added
	 * in the definition {@link DefinitionForecast} of the reading.
	 */
	EnergyResource energyReading();

	/**
	 * Time at which the energy reading was last reset (i.e. time of reference
	 * at which {@link #energyReading()} started at zero. Past values (past
	 * resets) should be added in the {@link DefinitionForecast} of this field.
	 */
	TimeResource resetTime();

	/**
	 * Type of meter:<br>
	 * 1: two-way meter without nonreturn<br>
	 * 2: consumption meter<br>
	 * 3: generation meter<br>
	 * >=10.000: custom values
	 */
	@Override
	IntegerResource type();

	@Override
	HeatPrice price();

	@Override
	HeatConnectionBox distributionBox();
}
