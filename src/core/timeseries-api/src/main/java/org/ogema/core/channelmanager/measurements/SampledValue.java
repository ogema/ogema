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
package org.ogema.core.channelmanager.measurements;

/**
 * Represents a sampled value. A sampled value always includes a quality and a timestamp attribute. The timestamp
 * represents the time when the sample was created.
 */
public final class SampledValue implements Comparable<SampledValue> {

	private final long timestamp;
	private final Value value;
	private final Quality quality;

	/**
	 * Copy constructor: Attempts to create a copy of another SampledValue, including
	 * a copy of all fields contained in the other value.
	 */
	public SampledValue(final SampledValue other) {
		try {
			this.value = other.getValue().clone();
		} catch (CloneNotSupportedException ex) {
			throw new RuntimeException(ex);
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
	public int compareTo(final SampledValue other) {
		return Long.compare(this.getTimestamp(), other.getTimestamp());
	}
	
	/**
	 * This is similar to a #clone-method, but returns the same instance if the value
	 * field has a primitive value (i.e. is of type {@link FloatValue}, {@link DoubleValue}, 
	 * {@link IntegerValue}, {@link LongValue}, {@link BooleanValue}, or {@link StringValue}).
	 */
	public SampledValue copyDefensively() {
		if (value instanceof FloatValue || value instanceof DoubleValue || value instanceof IntegerValue
				|| value instanceof LongValue || value instanceof BooleanValue || value instanceof StringValue)
			return this;
		return new SampledValue(this);
	}
	
}
