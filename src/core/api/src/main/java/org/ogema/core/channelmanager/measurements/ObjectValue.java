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
/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * OGEMA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * OGEMA. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ogema.core.channelmanager.measurements;

import org.ogema.core.timeseries.ReadOnlyTimeSeries;

/**
 *
 * Object{@link Value}
 *
 */
public class ObjectValue implements Value {

	private Object value;

	public ObjectValue(Object value) {
		this.value = value;
	}

	@Override
	public float getFloatValue() throws IllegalConversionException {
		throw new IllegalConversionException("Cannot convert a generic object to a float");
	}

	@Override
	public double getDoubleValue() throws IllegalConversionException {
		throw new IllegalConversionException("Cannot convert a generic object to a double");
	}

	@Override
	public int getIntegerValue() throws IllegalConversionException {
		throw new IllegalConversionException("Cannot convert a generic object to an integer");
	}

	@Override
	public long getLongValue() throws IllegalConversionException {
		throw new IllegalConversionException("Cannot convert a generic object to a long");
	}

	@Override
	public String getStringValue() throws IllegalConversionException {
		return value.toString();
	}

	@Override
	public byte[] getByteArrayValue() throws IllegalConversionException {
		throw new IllegalConversionException("Cannot convert a generic object to a byte array.");
	}

	@Override
	public boolean getBooleanValue() throws IllegalConversionException {
		throw new IllegalConversionException("Cannot convert a generic object to a boolean.");
	}

	@Override
	public Object getObjectValue() {
		return value;
	}

	@Override
	public Value clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException(
				"ObjectValue class has no sensible meaning of cloning since the type of object is arbitrary.");
	}

	@Override
	public ReadOnlyTimeSeries getTimeSeriesValue() throws IllegalConversionException {
		throw new IllegalConversionException("Cannot convert a generic object to a time series.");
	}
}
