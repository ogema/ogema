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
package org.ogema.driver.homematic.config;

public class HMList6 extends HMList {
	HMList list;

	public HMList6() {
		list = this;
		nl(6, 97, 0, "partyEndHr", 6, 0f, 23f, "", -1.0f, "h", true, "Party end hour. Use cmd partyMode to set");
		nl(6, 97, 7, "partyEndMin", 1, 0f, 1f, "lookup47", -1.0f, "min", true,
				"Party end min. Use cmd partyMode to set");
		nl(6, 98, 0, "partyEndDay", 8, 0f, 200f, "", -1.0f, "d", true, "Party duration days. Use cmd partyMode to set");
	}
}
