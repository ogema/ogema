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

import org.ogema.core.timeseries.ReadOnlyTimeSeries;

/**
 * 
 * Long{@linkplain Value}
 * 
 */
public class LongValue implements Value {

	private final long value;

	public LongValue(long value) {
		this.value = value;
	}

	@Override
	public float getFloatValue() throws IllegalConversionException {
		return (float) value;
	}

	@Override
	public double getDoubleValue() throws IllegalConversionException {
		return (double) value;
	}

	@Override
	public int getIntegerValue() throws IllegalConversionException {
		return (int) value;
	}

	@Override
	public long getLongValue() throws IllegalConversionException {
		return value;
	}

	@Override
	public String getStringValue() throws IllegalConversionException {
		return Long.toString(value);
	}

	@Override
	public byte[] getByteArrayValue() throws IllegalConversionException {
		throw new IllegalConversionException("Cannot convert a long to a byte array.");
	}

	@Override
	public boolean getBooleanValue() throws IllegalConversionException {
		if (value != 0)
			return true;
		else
			return false;
	}

	@Override
	public Object getObjectValue() {
		return new Long(value);
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof LongValue) && Long.compare(((LongValue) obj).value, value) == 0;
	}

	@Override
	public int hashCode() {
		return (int) value;
	}

	@Override
	public LongValue clone() {
		return new LongValue(value);
	}

	@Override
	public ReadOnlyTimeSeries getTimeSeriesValue() throws IllegalConversionException {
		throw new IllegalConversionException("Cannot convert a long vale to a time series.");
	}
}
