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
package org.ogema.model.smartgriddata;

import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.model.units.EnergyResource;
import org.ogema.model.prototypes.Data;
import org.ogema.model.ranges.PowerRange;

/**
 * Request for a certain amount of energy requested to be delivered (positive
 * value) or taken up (negative values). A time interval [startTime; endTime]
 * can be defined if the request shall be fulfilled within a particular time
 * frame.
 */
public interface GenericEnergyRequest extends Data {

	/**
	 * Amount of energy required.
	 */
	EnergyResource requiredEnergy();

	/**
	 * Starting time of the request. If this is not set, an active request is
	 * assumed to be requiring now.
	 */
	TimeResource startTime();

	/**
	 * End time of the request (technically, first ms after the request end). If
	 * this is not set, the request is assumed to be open until satisfied.
	 */
	TimeResource endTime();

	/**
	 * Maximum and minimum power settings that may be used to satisfy the request.
	 * If the limits depend on time, their {@link org.ogema.schedule.DefinitionSchedule DefinitionSchedule}
	 * sub-resources shall be used to model this.
	 */
	PowerRange powerLimits();
}
