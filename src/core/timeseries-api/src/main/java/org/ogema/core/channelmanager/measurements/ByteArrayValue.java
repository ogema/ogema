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

import java.util.Arrays;
import org.ogema.core.timeseries.ReadOnlyTimeSeries;

/**
 * 
 * A ByteArray{@link Value}
 * 
 */
public class ByteArrayValue implements Value {

	private byte[] value;

	/**
	 * Constructor
	 * 
	 * @param value
	 */
	public ByteArrayValue(byte[] value) {
		this.value = value;
	}

	@Override
	public float getFloatValue() throws IllegalConversionException {
		throw new IllegalConversionException("Cannot convert a byte array to a single float.");
	}

	@Override
	public double getDoubleValue() throws IllegalConversionException {
		throw new IllegalConversionException("Cannot convert a byte array to a single double.");
	}

	@Override
	public int getIntegerValue() throws IllegalConversionException {
		throw new IllegalConversionException("Cannot convert a byte array to a single integer.");
	}

	@Override
	public long getLongValue() throws IllegalConversionException {
		throw new IllegalConversionException("Cannot convert a byte array to a single long integer.");
	}

	@Override
	public String getStringValue() throws IllegalConversionException {
		throw new IllegalConversionException("Cannot convert a byte array to a string.");
	}

	@Override
	public byte[] getByteArrayValue() throws IllegalConversionException {
		return value;
	}

	@Override
	public boolean getBooleanValue() throws IllegalConversionException {
		throw new IllegalConversionException("Cannot convert a byte array to a single boolean.");
	}

	@Override
	public Object getObjectValue() {
		return value;
	}

	@Override
	public ByteArrayValue clone() throws CloneNotSupportedException {
		ByteArrayValue c = (ByteArrayValue) super.clone();
                c.value = value.clone();
                return c;
	}
	
	@Override
	public String toString() {
		return "ByteArrayValue" + Arrays.toString(value);
	}

	@Override
	public ReadOnlyTimeSeries getTimeSeriesValue() throws IllegalConversionException {
		throw new IllegalConversionException("Cannot convert a byte array to a time series.");
	}
}
