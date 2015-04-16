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
 * A Float{@link Value}
 * 
 */
public class FloatValue implements Value {

	private float value;

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
		return value;
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
