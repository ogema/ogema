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
package org.ogema.model.stakeholders;

import org.ogema.core.model.array.IntegerArrayResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.model.prototypes.Data;

/** Phone number in a machine processable form */
public interface PhoneNumber extends Data {
	/** Country code without starting "00" or "++" */
	IntegerArrayResource countryCode();

	/**
	 * Area code / prefix to be used within the country, without starting "0" or "1" that have to be left out when
	 * dialing with country code
	 */
	IntegerArrayResource cityCode();

	/** Local code including possibly the internal code */
	IntegerArrayResource localCode();

	/** Internal code to dial from within a company phone network etc. */
	IntegerArrayResource internalCode();

	/** Optionally also a string representation for the phone number may be given */
	StringResource standardDisplay();
}
