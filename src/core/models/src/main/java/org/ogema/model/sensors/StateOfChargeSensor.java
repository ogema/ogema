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
package org.ogema.model.sensors;

import org.ogema.core.model.ModelModifiers.NonPersistent;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.model.ranges.GenericFloatRange;
import org.ogema.model.targetranges.GenericFloatTargetRange;

/**
 * Sensor measuring the state of charge of something in relative units, from
 * 0.0 (uncharged) to 1.0 (fully charged). 
 */
public interface StateOfChargeSensor extends GenericFloatSensor {

	/**
	 * State of charge of the storage (0.0=empty, 1.0= fully loaded)<br>
	 * unit: 0.0 .. 1.0
	 */
	@NonPersistent
	@Override
	FloatResource reading();

	@Override
	GenericFloatRange ratedValues();

	/**
	 * Target settings as a state of charge (as in {@link #reading()}).
	 */
	@Override
	GenericFloatTargetRange settings();

	@Override
	GenericFloatTargetRange deviceSettings();

	@Override
	GenericFloatTargetRange deviceFeedback();

	//	/**
	//	 * Maximum rated storage capacity unit: J TODO may be changed, see
	//	 * documentation of class for more info.
	//	 */
	//	EnergyResource maxCapacityEnergy();
}
