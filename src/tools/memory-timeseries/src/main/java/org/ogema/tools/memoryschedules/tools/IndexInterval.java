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
package org.ogema.tools.memoryschedules.tools;

import java.util.List;

/**
 * Definition of an interval [min; max), i.e. with start point inclusive
 * and the end point not included.
 * 
 * @author Timo Fischer, Fraunhofer IWES
 */
public class IndexInterval {

	private int m_min, m_max;

	public IndexInterval() {
	}

	/**
	 * Creates an IndexInterval spanning all indices in the list.
	 */
	public IndexInterval(List<? extends Object> list) {
		m_min = 0;
		m_max = list.size();
	}

	/**
	 * Explicit constructor for the interval, where min and max are passed directly.
	 */
	public IndexInterval(int min, int max) {
		m_min = min;
		m_max = max;
	}

	public void setMin(int min) {
		m_min = min;
	}

	public void setMax(int max) {
		m_max = max;
	}

	public int getMin() {
		return m_min;
	}

	public int getMax() {
		return m_max;
	}

	/**
	 * Gets the mid-point of this interval (rounded down).
	 */
	public int mid() {
		return (m_min + m_max) / 2;
	}

	/**
	 * Returns the size of this interval.
	 */
	public int size() {
		return m_max - m_min;
	}

	/**
	 * Returns a new interval which represents the lower half of this.
	 */
	public IndexInterval lowerHalf() {
		return new IndexInterval(m_min, mid());
	}

	/**
	 * Returns a new interval which represents the upper half of this.
	 */
	public IndexInterval upperHalf() {
		return new IndexInterval(mid(), m_max);
	}
}
