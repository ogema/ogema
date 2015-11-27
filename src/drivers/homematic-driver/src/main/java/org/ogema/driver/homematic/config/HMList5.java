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

public class HMList5 extends HMList {
	HMList list;

	public HMList5() {
		list = this;
		nl(5, 1, 0, "displayMode", 1, 0f, 1f, "lookup41", -1.0f, "", true, "");
		nl(5, 1, 1, "displayTemp", 1, 0f, 1f, "lookup42", -1.0f, "", true, "");
		nl(5, 1, 2, "displayTempUnit", 1, 0f, 1f, "lookup43", -1.0f, "", true, "");
		nl(5, 1, 3, "controlMode", 2, 0f, 3f, "lookup44", -1.0f, "", true, "");
		nl(5, 1, 5, "decalcDay", 3, 0f, 7f, "lookup45", -1.0f, "", false, "Decalc weekday");
		nl(5, 10, 0, "valveErrorPos", 8, 0f, 99f, "", -1.0f, "%", true, "Valve position when error");// size actually
		// 0.7
		nl(5, 2, 6, "mdTempValve", 2, 0f, 2f, "lookup46", -1.0f, "", true, "");
		nl(5, 3, 0, "day-temp", 6, 6f, 30f, "", 2f, "C", true, "comfort or day temperatur");
		nl(5, 4, 0, "night-temp", 6, 6f, 30f, "", 2f, "C", true, "lower or night temperatur");
		nl(5, 5, 0, "tempWinOpen", 6, 6f, 30f, "", 2f, "C", true, "Temperature for Win open");
		nl(5, 6, 0, "party-temp", 6, 6f, 30f, "", 2f, "C", true, "Temperature for Party");
		nl(5, 8, 0, "decalMin", 3, 0f, 50f, "", 0.1f, "min", false, "Decalc min");
		nl(5, 8, 3, "decalHr", 5, 0f, 23f, "", -1.0f, "h", false, "Decalc hour");
		nl(5, 9, 0, "valveOffset", 5, 0f, 25f, "", -1.0f, "%", true, "Valve offset");// size actually 0.5
	}
}
