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
 * String{@link Value}
 * 
 */
public class StringValue implements Value {

	private final String value;

	public StringValue(String value) {
		this.value = value;
	}

	@Override
	public float getFloatValue() throws IllegalConversionException {
		throw new IllegalConversionException("Cannot convert a generic string to a float.");
	}

	@Override
	public double getDoubleValue() throws IllegalConversionException {
		throw new IllegalConversionException("Cannot convert a generic string to a double.");
	}

	@Override
	public int getIntegerValue() throws IllegalConversionException {
		throw new IllegalConversionException("Cannot convert a generic string to an integer.");
	}

	@Override
	public long getLongValue() throws IllegalConversionException {
		throw new IllegalConversionException("Cannot convert a generic string to a long.");
	}

	@Override
	public String getStringValue() throws IllegalConversionException {
		return value;
	}

	@Override
	public byte[] getByteArrayValue() throws IllegalConversionException {
		throw new IllegalConversionException("Cannot convert a generic string to a byte array.");
	}

	@Override
	public boolean getBooleanValue() throws IllegalConversionException {
		throw new IllegalConversionException("Cannot convert a generic string to a boolean.");
	}

	@Override
	public Object getObjectValue() {
		return value;
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof StringValue) && (((StringValue) obj).value.equals(value));
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public StringValue clone() throws CloneNotSupportedException {
		return (StringValue) super.clone();
	}

	@Override
	public String toString() {
		return "StringValue[" + value + "]";
	}
	
	@Override
	public ReadOnlyTimeSeries getTimeSeriesValue() throws IllegalConversionException {
		throw new IllegalConversionException("Cannot convert a generic string to a time series.");
	}

}
