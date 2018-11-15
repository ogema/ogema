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
