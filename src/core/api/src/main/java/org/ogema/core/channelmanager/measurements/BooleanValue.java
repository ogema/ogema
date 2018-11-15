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
 * A Boolean{@link Value}
 * 
 */
public class BooleanValue implements Value {

	public final static BooleanValue TRUE = new BooleanValue(true);
	public final static BooleanValue FALSE = new BooleanValue(false);
	
	private final boolean value;

	public BooleanValue(boolean value) {
		this.value = value;
	}

	@Override
	public float getFloatValue() throws IllegalConversionException {
		return (float) getIntegerValue();
	}

	@Override
	public double getDoubleValue() throws IllegalConversionException {
		return (double) getIntegerValue();
	}

	@Override
	public int getIntegerValue() throws IllegalConversionException {
		if (value == true)
			return 1;
		else
			return 0;
	}

	@Override
	public long getLongValue() throws IllegalConversionException {
		return (long) getIntegerValue();
	}

	@Override
	public String getStringValue() throws IllegalConversionException {
		return Boolean.toString(value);
	}

	@Override
	public byte[] getByteArrayValue() throws IllegalConversionException {
		throw new IllegalConversionException("Cannot convert a boolean to a byte array.");
	}

	@Override
	public boolean getBooleanValue() throws IllegalConversionException {
		return value;
	}

	@Override
	public Object getObjectValue() {
		return new Boolean(value);
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof BooleanValue) && Boolean.compare(((BooleanValue) obj).value, value) == 0;
	}

	@Override
	public int hashCode() {
		return value ? 1 : 0;
	}

	@Override
	public BooleanValue clone() {
		return new BooleanValue(value);
	}

	@Override
	public ReadOnlyTimeSeries getTimeSeriesValue() throws IllegalConversionException {
		throw new IllegalConversionException("Cannot convert a boolean to a time series.");
	}

}
