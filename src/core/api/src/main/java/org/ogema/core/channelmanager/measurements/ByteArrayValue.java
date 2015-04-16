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
 * A ByteArray{@link Value}
 * 
 */
public class ByteArrayValue implements Value {

	private byte[] value;

	/**
	 * Contructor
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
	public ByteArrayValue clone() {
		return new ByteArrayValue(value.clone());
	}

	@Override
	public ReadOnlyTimeSeries getTimeSeriesValue() throws IllegalConversionException {
		throw new IllegalConversionException("Cannot convert a byte array to a time series.");
	}
}
