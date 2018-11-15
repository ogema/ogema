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

/**
 * Resource representing a flow..
 */
public interface FlowResource extends PhysicalUnitResource {

	/**
	 * Returns the flow in cubic meters per second.
	 *
	 * @see FloatResource#getValue()
	 */
	@Override
	float getValue();

	/**
	 * Sets the flow to a new value. Unit is cubic meters per second.
	 *
	 * @see FloatResource#getValue()
	 */
	@Override
	boolean setValue(float value);

	/**
	 * Returns "m³/s".
	 *
	 * @see PhysicalUnitResource#getUnit()
	 */
	@Override
	PhysicalUnit getUnit();

	/**
	 * Sets the value in m³/h. The value stored in the resource is converted to m³/s.
	 * 
	 * @see FlowResource#getCubicMeterPerHour()
	 */
	boolean setCubicMeterPerHour(float value);

	/**
	 * Get the current flux value in m³/h.
	 * 
	 * @see FlowResource#setCubicMeterPerHour(float)
	 */
	float getCubicMeterPerHour();
}
