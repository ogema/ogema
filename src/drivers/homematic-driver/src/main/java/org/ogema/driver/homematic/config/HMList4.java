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
