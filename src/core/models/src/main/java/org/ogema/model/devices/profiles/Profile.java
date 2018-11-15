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
