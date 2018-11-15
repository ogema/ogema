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
package org.ogema.model.metering;

import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.model.devices.connectiondevices.GenericConnectionBox;
import org.ogema.model.prototypes.Connection;
import org.ogema.model.prototypes.PhysicalElement;
import org.ogema.model.smartgriddata.Price;

/**
 * Generic meter for a commodity. If a metering device has multiple registers
 * that cannot be covered with a single meter resource, use separate meter
 * resources for each register separately. Concrete meters will inherit from this basis
 * model (and include fields for the meter readings).
 */
public interface GenericMeter extends PhysicalElement {

	/**
	 * Connection for measured commodity
	 */
	Connection connection();

	/**
	 * Unique meter identifier (usually provided by metering service provider)
	 */
	StringResource meterId();

	/**
	 * Metering type: Common identifiers for all types of meters are:<br>
	 * 1: two-way meter without nonreturn<br>
	 * 2: consumption meter<br>
	 * 3: generation meter<br>
	 * greater/equal 10000: range left open for user-defined values.<br>
	 * Specialized meters deriving from this class can define additional types
	 * in the range smaller 10000.
	 */
	IntegerResource type();

	/**
	 * Price for commodity measured on this meter
	 */
	Price price();

	/**
	 * True: The meter is a local sub-meter. The actual billing is
	 * according to the relevant main meter
	 */
	BooleanResource isSubMeter();

	/**
	 * Reference to distribution box in which device is installed, if applicable
	 */
	GenericConnectionBox distributionBox();
}
