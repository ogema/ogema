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
 * A Double{@link Value}
 * 
 */
public class DoubleValue implements Value {

	public static final DoubleValue NAN = new DoubleValue(Double.NaN);
	public static final DoubleValue ZERO = new DoubleValue(0);
	
	private final double value;

	public DoubleValue(double value) {
		this.value = value;
	}

	@Override
	public float getFloatValue() throws IllegalConversionException {
		return (float) value;
	}

	@Override
	public double getDoubleValue() throws IllegalConversionException {
		return value;
	}

	@Override
	public int getIntegerValue() throws IllegalConversionException {
		return (int) value;
	}

	@Override
	public long getLongValue() throws IllegalConversionException {
		return (long) value;
	}

	@Override
	public String getStringValue() throws IllegalConversionException {
		return Double.toString(value);
	}

	@Override
	public byte[] getByteArrayValue() throws IllegalConversionException {
		throw new IllegalConversionException("Cannot convert a double value to a byte array.");
	}

	@Override
	public boolean getBooleanValue() throws IllegalConversionException {
            return value != 0.f;
	}

	@Override
	public Object getObjectValue() {
		return value;
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof DoubleValue) && Double.compare(((DoubleValue) obj).value, value) == 0;
	}

	@Override
	public int hashCode() {
		return (int) value;
	}

	@Override
	public DoubleValue clone() throws CloneNotSupportedException {
		return (DoubleValue) super.clone();
	}

	@Override
	public String toString() {
		return "DoubleValue[" + value + "]";
	}
	
	@Override
	public ReadOnlyTimeSeries getTimeSeriesValue() throws IllegalConversionException {
		throw new IllegalConversionException("Cannot convert a double value to a time series.");
	}
}
