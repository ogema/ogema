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
