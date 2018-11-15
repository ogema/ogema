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
 * A Float{@link Value}
 * 
 */
public class FloatValue implements Value {

	public static final FloatValue NAN = new FloatValue(Float.NaN);
	public static final FloatValue ZERO = new FloatValue(0);
	
	private final float value;

	public FloatValue(float value) {
		this.value = value;
	}

	@Override
	public float getFloatValue() throws IllegalConversionException {
		return value;
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
		return (long) value;
	}

	@Override
	public String getStringValue() throws IllegalConversionException {
		return Float.toString(value);
	}

	@Override
	public byte[] getByteArrayValue() throws IllegalConversionException {
		throw new IllegalConversionException("Cannot convert a float to a byte array.");
	}

	@Override
	public boolean getBooleanValue() throws IllegalConversionException {
		throw new IllegalConversionException("Cannot convert a float value to a boolean.");
	}

	@Override
	public Object getObjectValue() {
		return new Float(value);
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof FloatValue) && Float.compare(((FloatValue) obj).value, value) == 0;
	}

	@Override
	public int hashCode() {
		return (int) value;
	}

	@Override
	public FloatValue clone() {
		return new FloatValue(value);
	}

	@Override
	public ReadOnlyTimeSeries getTimeSeriesValue() throws IllegalConversionException {
		throw new IllegalConversionException("Cannot convert a float value to a time series");
	}

}
