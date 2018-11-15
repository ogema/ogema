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
