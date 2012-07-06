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
package org.ogema.model.sensors;

import org.ogema.core.model.ModelModifiers.NonPersistent;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.model.ranges.BinaryRange;
import org.ogema.model.targetranges.BinaryTargetRange;

/**
 * Occupancy sensor
 */
public interface OccupancySensor extends Sensor {

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
