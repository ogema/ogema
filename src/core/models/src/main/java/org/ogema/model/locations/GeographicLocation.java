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
package org.ogema.model.locations;

import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.model.prototypes.Data;

/**
 * The geographic location of a vicinity, given in longitude and latitude.
 */
public interface GeographicLocation extends Data {
	/**
	 * Longitude, full degrees<br>
	 * positive: east negative: west
	 */
	IntegerResource longitudeFullDegrees();

	/**
	 * longitude, minutes of arc unit: Minutes of Arc
	 */
	FloatResource longitudeArcMinutes();

	/**
	 * Latitude, full degrees<br>
	 * positive: north, negative: south
	 */
	IntegerResource latitudeFullDegrees();

	/**
	 * latitude, minutes of arc unit: Minutes of Arc
	 */
	FloatResource latitudeArcMinutes();
}
