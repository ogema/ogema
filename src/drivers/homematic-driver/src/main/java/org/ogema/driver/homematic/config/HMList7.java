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

public class HMList7 extends HMList {
	HMList list;

	public HMList7() {
		list = this;
		nl(7, 1, 0, "dayTemp", 6, 15f, 30f, "", 2f, "C", true, "comfort or day temperatur");
		nl(7, 10, 0, "boostPos", 5, 0f, 100f, "", 0.2f, "%", true, "valve boost position");
		nl(7, 10, 5, "boostPeriod", 3, 0f, 6f, "lookup57", 5f, "min", false, "boost period [min]");
		nl(7, 11, 0, "valveOffsetRt", 7, 0f, 100f, "", -1.0f, "%", true, "offset for valve");
		nl(7, 12, 0, "valveMaxPos", 7, 0f, 100f, "", -1.0f, "%", false, "valve maximum position");
		nl(7, 13, 0, "valveErrPos", 7, 0f, 100f, "", -1.0f, "%", false, "valve error position");
		nl(7, 14, 0, "daylightSaveTime", 1, 0f, 1f, "lookup1", -1.0f, "", true, "set daylight saving time");
		nl(7, 14, 1, "regAdaptive", 2, 0f, 2f, "lookup51", -1.0f, "", true,
				"adaptive regu on or off with default or determined values");
		nl(7, 14, 3, "showInfo", 2, 0f, 1f, "lookup48", -1.0f, "", true, "show date or time");
		nl(7, 14, 5, "winOpnBoost", 1, 0f, 1f, "lookup1", -1.0f, "", true, "boost after window closed");
		nl(7, 14, 6, "noMinMax4Manu", 1, 0f, 1f, "lookup1", -1.0f, "", true, "min/max is irrelevant for manual mode");
		nl(7, 14, 7, "showWeekday", 1, 0f, 1f, "lookup1", -1.0f, "", false, "show weekday");
		nl(7, 15, 0, "hyst2point", 5, 0f, 2f, "", 10f, "C", true, "hysteresis range");
		nl(7, 15, 7, "heatCool", 1, 0f, 1f, "lookup49", -1.0f, "", true, "select heating or cooling");
		nl(7, 16, 0, "weekPrgSel", 8, 0f, 2f, "lookup50", -1.0f, "", true, "select week program");
		nl(7, 18, 0, "modePrioParty", 3, 0f, 5f, "lookup52", -1.0f, "", true, "allow tempChange for party only by: ");
		nl(7, 18, 3, "modePrioManu", 3, 0f, 5f, "lookup52", -1.0f, "", true, "allow tempChange for manual only by: ");
		nl(7, 19, 0, "winOpnDetFall", 5, 0.5f, 2.5f, "", 10f, "K", false,
				"detect Window Open if temp falls more then...");
		nl(7, 19, 5, "winOpnMode", 3, 0f, 4f, "lookup53", -1.0f, "", false, "enable internal Windoe open in modes: ");
		nl(7, 2, 0, "nightTemp", 6, 5f, 25f, "", 2f, "C", true, "lower or night temperatur");
		nl(7, 202, 0, "reguIntI", 8, 10f, 20f, "", -1.0f, "", false, "regulator I-param internal mode");
		nl(7, 203, 0, "reguIntP", 8, 25f, 35f, "", -1.0f, "", false, "regulator P-param internal mode");
		nl(7, 204, 0, "reguIntPstart", 8, 5f, 45f, "", -1.0f, "", false, "regulator P-param internal mode start value");
		nl(7, 205, 0, "reguExtI", 8, 10f, 20f, "", -1.0f, "", false, "regulator I-param extern mode");
		nl(7, 206, 0, "reguExtP", 8, 25f, 35f, "", -1.0f, "", false, "regulator P-param extern mode");
		nl(7, 207, 0, "reguExtPstart", 8, 5f, 45f, "", -1.0f, "", false, "regulator P-param extern mode start value");
		nl(7, 3, 0, "tempMin", 6, 4.5f, 14.5f, "", 2f, "C", false, "minimum temperatur");
		nl(7, 4, 0, "tempMax", 6, 15f, 30.5f, "", 2f, "C", false, "maximum temperatur");
		nl(7, 5, 0, "winOpnTemp", 6, 5f, 30f, "", 2f, "C", false, "lowering temp whenWindow is opened");
		nl(7, 6, 0, "winOpnPeriod", 4, 0f, 60f, "", 0.2f, "min", false, "period lowering when window is open");
		nl(7, 7, 0, "decalcWeekday", 3, 0f, 7f, "lookup45", -1.0f, "", false, "decalc at day");
		nl(7, 8, 0, "decalcTime", 6, 0f, 1410f, "min2time", -1.0f, "", false, "decalc at hour");
		nl(7, 9, 0, "tempOffset", 4, 0f, 15f, "lookup54", -1.0f, "", true, "temperature offset");
		nl(7, 9, 4, "btnNoBckLight", 1, 0f, 1f, "lookup1", -1.0f, "", true, "button response without backlight");
		nl(7, 9, 5, "showSetTemp", 1, 0f, 1f, "lookup55", -1.0f, "", true, "show set or actual temperature");
		nl(7, 9, 6, "showHumidity", 1, 0f, 1f, "lookup56", -1.0f, "", true, "show temp only or also humidity");
		nl(7, 9, 7, "sendWeatherData", 1, 0f, 1f, "lookup1", -1.0f, "", true, "send  weather data");
	}
}
