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
