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
package org.ogema.channels.tests.utils;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

public class RangeMatcher extends BaseMatcher<Integer> {

	private final int rangeStart;
	private final int rangeEnd;

	public RangeMatcher(int rangeStart, int rangeEnd) {
		this.rangeStart = rangeStart;
		this.rangeEnd = rangeEnd;
	}

	public static RangeMatcher inRange(int start, int end) {
		return new RangeMatcher(start, end);
	}

	@Override
	public boolean matches(Object arg0) {
		if (arg0 instanceof Integer) {
			int value = (Integer) arg0;

			if ((value >= rangeStart) && (value <= rangeEnd)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void describeTo(Description arg0) {
		arg0.appendText("range <" + rangeStart + ">...<" + rangeEnd + ">");
	}

}
