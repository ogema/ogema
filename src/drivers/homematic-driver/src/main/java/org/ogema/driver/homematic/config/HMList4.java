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

public class HMList4 extends HMList {
	HMList list;

	public HMList4() {
		list = this;
		nl(4, 1, 0, "peerNeedsBurst", 1, 0f, 1f, "lookup1", -1.0f, "", true, "peer expects burst");
		nl(4, 1, 7, "expectAES", 1, 0f, 1f, "lookup1", -1.0f, "", true, "expect AES");
		nl(4, 2, 0, "lcdSymb", 1, 0f, 8f, "lookup39", -1.0f, "", true, "symbol to display on message");
		nl(4, 3, 0, "lcdLvlInterp", 1, 0f, 5f, "lookup40", -1.0f, "", true, "bitmask for symbols");
		nl(4, 4, 0, "fillLvlUpThr", 8, 0f, 255f, "", -1.0f, "", true, "fill level upper threshold");
		nl(4, 5, 0, "fillLvlLoThr", 8, 0f, 255f, "", -1.0f, "", true, "fill level lower threshold");
	}
}
