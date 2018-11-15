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
package org.ogema.model.devices.connectiondevices;

import org.ogema.model.locations.Building;
import org.ogema.model.locations.BuildingPropertyUnit;
import org.ogema.model.prototypes.Connection;
import org.ogema.model.prototypes.PhysicalElement;

/**
 * Connection and/or distribution element for a commodity, for example an electric distribution box, a thermal circuit of water
 * circuit branching point etc.
 */
public interface GenericConnectionBox extends PhysicalElement {
	/**
	 * The connection that this box represents and acts on.
	 */
	Connection connection();

	/**
	 * Reference to building property unit which the connection box connects a public grid, if applicable
	 */
	BuildingPropertyUnit propertyUnit();

	/**
	 * Reference to building which the connection box connects a public grid to, if applicable
	 */
	Building building();
}
