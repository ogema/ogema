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
 * Integer{@linkplain Value}
 * 
 */
public class IntegerValue implements Value {

	public final static IntegerValue ZERO = new IntegerValue(0);
	public final static IntegerValue ONE = new IntegerValue(1);
	private final int value;

	public IntegerValue(int value) {
		this.value = value;
	}

	@Override
	public float getFloatValue() {
		return (float) value;
	}

	@Override
	public double getDoubleValue() {
		return (double) value;
	}

	@Override
	public int getIntegerValue() {
		return value;
	}

	@Override
	public long getLongValue() {
		return (long) value;
	}

	@Override
	public String getStringValue() {
		return Integer.toString(value);
	}

	@Override
	public byte[] getByteArrayValue() throws IllegalConversionException {
		throw new IllegalConversionException("Cannot convert an integer to a byte array.");
	}

	@Override
	public boolean getBooleanValue() {
            return value != 0;
	}

	@Override
	public Object getObjectValue() {
		return value;
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof IntegerValue) && Integer.compare(((IntegerValue) obj).value, value) == 0;
	}

	@Override
	public int hashCode() {
		return value;
	}

	@Override
	public IntegerValue clone() throws CloneNotSupportedException {
		return (IntegerValue) super.clone();
	}

	@Override
	public String toString() {
		return "IntegerValue[" + value + "]";
	}
	
	@Override
	public ReadOnlyTimeSeries getTimeSeriesValue() throws IllegalConversionException {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

}
