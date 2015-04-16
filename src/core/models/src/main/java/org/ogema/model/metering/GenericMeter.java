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
	 * Measurement values are part of the connection.
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
	 * >=10000: range left open for user-defined values.<br>
	 * Specialized meters deriving from this class can define additional types
	 * in the range <10000.
	 */
	IntegerResource type();

	/**
	 * Price for power measured on this meter
	 */
	Price price();

	/**
	 * True: The meter is a local sub-meter. The energy measured is billed
	 * according to the relevant main meter
	 */
	BooleanResource isSubMeter();

	/**
	 * Reference to distribution box in which device is installed
	 */
	GenericConnectionBox distributionBox();
}
