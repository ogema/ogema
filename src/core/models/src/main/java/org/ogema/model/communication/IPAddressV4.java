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

import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.model.prototypes.Data;

/**
 * An IP V4 address.
 */
public interface IPAddressV4 extends Data {
	/** Address as URL or IP address in 4xnumber-3xdot-format */
	public StringResource address();

	/** Address may additionally be provided as long */
	public TimeResource ipAddress();

	/** Port if specified */
	public IntegerResource port();

	/**
	 * MAC address attached to the IPAddress (from the perspective of the gateway) may be added if known/relevant
	 */
	public StringResource macAddress();
}
