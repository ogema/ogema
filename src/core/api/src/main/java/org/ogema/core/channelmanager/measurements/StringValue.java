/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
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
		throw new IllegalConversionException();
	}

	@Override
	public double getDoubleValue() throws IllegalConversionException {
		throw new IllegalConversionException();
	}

	@Override
	public int getIntegerValue() throws IllegalConversionException {
		throw new IllegalConversionException();
	}

	@Override
	public long getLongValue() throws IllegalConversionException {
		throw new IllegalConversionException();
	}

	@Override
	public String getStringValue() throws IllegalConversionException {
		return value;
	}

	@Override
	public byte[] getByteArrayValue() throws IllegalConversionException {
		throw new IllegalConversionException();
	}

	@Override
	public boolean getBooleanValue() throws IllegalConversionException {
		throw new IllegalConversionException();
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

}
