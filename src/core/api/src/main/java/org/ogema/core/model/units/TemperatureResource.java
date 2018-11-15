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
