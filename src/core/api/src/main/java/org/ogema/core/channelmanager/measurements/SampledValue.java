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
package org.ogema.core.channelmanager.measurements;

/**
 * Represents a sampled value. A sampled value always includes a quality and a timestamp attribute. The timestamp
 * represents the time when the sample was created.
 */
public class SampledValue implements Comparable<SampledValue> {

	private long timestamp;
	private Value value;
	private Quality quality;

	/**
	 * Copy constructor: Attempts to create a copy of another SampledValue, including
	 * a copy of all fields contained in the other value.
	 */
	public SampledValue(final SampledValue other) {
		try {
			this.value = other.getValue().clone();
		} catch (CloneNotSupportedException ex) {
			ex.printStackTrace();
		}
		this.timestamp = other.getTimestamp();
		this.quality = other.getQuality();
	}

	/**
	 * Creates a new SampledValue that wraps a given measurement value with
	 * a timestamp and an associated quality.
	 * 
	 * @param value measurement value
	 * @param timestamp time associated to the measurement
	 * @param quality Quality of the measurement
	 */
	public SampledValue(Value value, long timestamp, Quality quality) {
		this.value = value;
		this.timestamp = timestamp;
		this.quality = quality;
	}

	/**
	 * Gets the measurement value.
	 */
	public Value getValue() {
		return value;
	}

	/** 
	 * Gets the timestamp.
	 */
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * Gets the Quality of the measurement value.
	 */
	public Quality getQuality() {
		return quality;
	}

	/**
	 * Checks equality with another object. 
	 * @param obj Object to check equality with.
	 * @return This is equal to obj if obj is a SampledValue with same quality,
	 * same timestamp and an equal measurement value (returns true). Otherwise,
	 * this is not equal to obj (returns false).
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof SampledValue)) {
			return false;
		}
		SampledValue other = (SampledValue) obj;
		return other.timestamp == timestamp && other.quality == quality && other.value.equals(value);
	}

	@Override
	public int hashCode() {
		return ((int) timestamp) ^ value.hashCode();
	}

	@Override
	public String toString() {
		return String.format("%s(%s)", value.getClass().getSimpleName(), value.getObjectValue());
	}

	/**
	 * Compares the timestampt of this with another sampled value's timestamp. 
	 * This implicitly defines a time ordering for SampledValues.
	 * @param o other value to compare to.
	 * @return -1 if the timestamp of this preceeds that of o. +1 if the timestamp
	 * of o preceeds that of this. 0 if both timestamps are equal.
	 */
	@Override
	public int compareTo(SampledValue o) {
		if (timestamp < o.getTimestamp())
			return -1;
		if (timestamp > o.getTimestamp())
			return +1;
		return 0;
	}
}
