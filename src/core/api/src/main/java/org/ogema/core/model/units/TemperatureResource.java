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
package org.ogema.core.model.units;

import org.ogema.core.model.simple.FloatResource;

public interface TemperatureResource extends PhysicalUnitResource {

	/**
	 * Returns the temperature in Kelvin.
	 *
	 * @see FloatResource#getValue()
	 */
	@Override
	float getValue();

	/**
	 * Sets the temperature to a new value. Unit is Kelvin.
	 *
	 * @see FloatResource#getValue()
	 */
	@Override
	boolean setValue(float value);

	/**
	 * Returns "K".
	 *
	 * @see PhysicalUnitResource#getUnit()
	 */
	@Override
	PhysicalUnit getUnit();

	/**
	 * Sets the new temperature in Celsius. Note that the value stored in the
	 * resource will be in the default unit for this.
	 */
	boolean setCelsius(float value);

	/**
	 * Gets the current temperature in degree Celsius (°C).
	 */
	float getCelsius();

	/**
	 * Sets the new temperature in Kelvin. Same as setValue().
	 */
	boolean setKelvin(float value);

	/**
	 * Gets the current temperature in Kelvin. Same as getValue().
	 */
	float getKelvin();
}
