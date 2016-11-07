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
package org.ogema.model.devices.profiles;

import org.ogema.core.model.ResourceList;
import org.ogema.core.model.schedule.RelativeSchedule;
import org.ogema.model.prototypes.Data;
import org.ogema.model.sensors.Sensor;

/**
 * Repeatable time profile of some physical quantity, for instance the consumption profile of an electrical
 * device in a specific operating mode.
 */
public interface Profile extends Data {

	/**
	 * (Reference to) the sensor that represents the quantity modeled by this profile
	 */
	Sensor sensor();
	
	/**
	 * Reference to a decorator of the {@link Sensor#reading()} subresource of {@link #sensor()} which contains
	 * the actual profile.
	 * 
	 * @deprecated use {@link #profileData()} instead. It supports multiple measurements for a single quantity. 
	 * 		Will be removed in OGEMA 2.0.6
	 */
	@Deprecated
	RelativeSchedule profile();
	
	/**
	 * Contains the actual time series. 
	 */
	ResourceList<ProfileData> profileData();

}
