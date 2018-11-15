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
