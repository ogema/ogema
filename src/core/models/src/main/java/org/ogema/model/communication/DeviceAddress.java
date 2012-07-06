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
package org.ogema.model.communication;

import org.ogema.core.model.simple.BooleanResource;
import org.ogema.model.prototypes.Data;

/** Collection of addressing schemes. */
public interface DeviceAddress extends Data {
	/** IPv4 address if applicable */
	IPAddressV4 ipV4Address();

	/** KNX address if applicable */
	KNXAddress knxAddress();

	/** ZigBee address if applicable */
	ZigBeeAddress zigBeeAddress();

	/**
	 * True : writable <br>
	 * False : not writable.
	 */
	BooleanResource writeable();

	/**
	 * True : readable <br>
	 * False : not readable.
	 */
	BooleanResource readable();
}
