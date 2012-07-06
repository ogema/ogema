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
package org.ogema.channelmanager.impl;

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
