/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3
 * as published by the Free Software Foundation.
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
	public StringValue clone() {
		return new StringValue(value);
	}

	@Override
	public ReadOnlyTimeSeries getTimeSeriesValue() throws IllegalConversionException {
		throw new IllegalConversionException("Cannot convert a generic string to a time series.");
	}

}
