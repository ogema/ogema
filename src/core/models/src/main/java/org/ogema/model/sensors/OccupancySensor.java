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
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.model.ranges.BinaryRange;
import org.ogema.model.targetranges.BinaryTargetRange;

/**
 * Occupancy sensor
 */
public interface OccupancySensor extends GenericBinarySensor {

	/**
	 * sensor reading detecting a room to be occupied<br>
	 * false: unoccupied true: occupied
	 */
	@NonPersistent
	@Override
	BooleanResource reading();

	/**
	 * possible sensor readings for a room to be occupied<br>
	 * false: unoccupied true: occupied
	 */
	@Override
	BinaryRange ratedValues();

	/**
	 * Settings for a room to be occupied<br>
	 * false: unoccupied true: occupied
	 */
	@Override
	BinaryTargetRange settings();

	/**
	 * Settings for a room to be occupied to be sent to the device<br>
	 * false: unoccupied true: occupied
	 */
	@Override
	BinaryTargetRange deviceSettings();

	/**
	 * Settings for a room to be occupied (feedback from the device)<br>
	 * false: unoccupied true: occupied
	 */
	@Override
	BinaryTargetRange deviceFeedback();

	/**
	 * Number of occupants in the room, possibly estimated
	 */
	@NonPersistent
	IntegerResource personNum();
}
