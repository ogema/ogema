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
package org.ogema.model.devices.sensoractordevices;

import org.ogema.core.model.units.LengthResource;
import org.ogema.model.prototypes.PhysicalElement;
import org.ogema.model.sensors.GeographicDirectionSensor;
import org.ogema.model.sensors.VelocitySensor;

/**
 * A sensor for wind speed and direction.
 */
public interface WindSensor extends PhysicalElement {

	/**
	 * Wind speed. Values shall always be positive and refer into the
	 * direction set by {@link #direction() }.
	 */
	VelocitySensor speed();

	/**
	 * Wind direction (direction from which wind is blowing)
	 */
	GeographicDirectionSensor direction();

	/**
	 * altitude above ground the measurement is taken.
	 */
	LengthResource altitude();
}
