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
 * Integer{@linkplain Value}
 * 
 */
public class IntegerValue implements Value {

	private int value;

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
		if (value != 0)
			return true;
		else
			return false;
	}

	@Override
	public Object getObjectValue() {
		return new Integer(value);
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
	public IntegerValue clone() {
		return new IntegerValue(value);
	}

	@Override
	public ReadOnlyTimeSeries getTimeSeriesValue() throws IllegalConversionException {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

}
