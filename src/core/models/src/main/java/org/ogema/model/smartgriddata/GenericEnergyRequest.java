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
	 * If the limits depend on time, their 
	 * {@link org.ogema.core.model.simple.FloatResource#program() powerLimits.lower/upperLimit.program}
	 * sub-resources shall be used to model this.
	 */
	PowerRange powerLimits();
}
