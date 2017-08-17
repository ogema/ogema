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
