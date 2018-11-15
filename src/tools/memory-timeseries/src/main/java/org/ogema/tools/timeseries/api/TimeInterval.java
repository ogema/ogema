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
package org.ogema.tools.timeseries.api;

/**
 * A time interval [t0; t1), where timestamps are long-values.
 */
public class TimeInterval implements Comparable<TimeInterval> {

	protected final long t0, t1;

	public TimeInterval(long t0, long t1) {
		this.t0 = t0;
		this.t1 = t1;
	}

	/**
	 * Get the intersection between two intervals
	 * @param other interval to intersect this with
	 * @return intersection between and the other interval. If there is no intersection, an empty interval is returned.
	 */
	public TimeInterval intersect(TimeInterval other) {
		final long t0result = (t0 > other.t0) ? t0 : other.t0;
		final long t1result = (t1 < other.t1) ? t1 : other.t1;
		if (t1result <= t0result) {
			return new TimeInterval(0, 0); // empty interval
		}
		return new TimeInterval(t0result, t1result);
	}

	/**
	 * Checks if a time stamp lies within the range of this interval.
	 * @param t timestamp in ms to check.
	 * @return true exactly if t lies in [t0; t1).
	 */
	public boolean timestampContained(long t) {
		return (t >= t0 && t < t1);
	}

	/**
	 * Checks if this is an empty interval
	 * @return true if the size of the time interval is zero ms, false if at least one ms is covered.
	 */
	public boolean isEmpty() {
		return t0 >= t1;
	}

	/**
	 * Checks if this interval actually exists.
	 * @return true exactly if at least one ms is covered by this interval.
	 */
	public boolean exists() {
		return t0 < t1;
	}

	/**
	 * Gets the starting time of the interval
	 * @return first ms in the time interval.
	 */
	public long getStart() {
		return t0;
	}

	/**
	 * Gets the ending time of the interval
	 * @return first ms that is not in the interval anymore.
	 */
	public long getEnd() {
		return t1;
	}

	@Override
	public int compareTo(TimeInterval interval) {
		if (t0 < interval.t0) {
			return -1;
		}
		if (t0 > interval.t0) {
			return +1;
		}
		if (t1 < interval.t1) {
			return -1;
		}
		if (t1 > interval.t1) {
			return +1;
		}
		return 0;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof TimeInterval))
			return false;
		final TimeInterval interval = (TimeInterval) obj;
		return (t0 == interval.t0 && t1 == interval.t1);
	}

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + (int) (this.t0 ^ (this.t0 >>> 32));
        hash = 59 * hash + (int) (this.t1 ^ (this.t1 >>> 32));
        return hash;
    }

}
