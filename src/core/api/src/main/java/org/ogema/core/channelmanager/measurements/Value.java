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
 * Value type for the ChannelAPI.
 */
public interface Value extends Cloneable {

	/**
	 * Get the value as a float variable.
	 * 
	 * @return float representation of the value
	 * @throws IllegalConversionException
	 *             if the value cannot be converted to a float.
	 */
	public float getFloatValue() throws IllegalConversionException;

	/**
	 * Get the value as a double variable.
	 * 
	 * @return double representation of the value
	 * @throws IllegalConversionException
	 *             if the value cannot be converted to a double.
	 */
	public double getDoubleValue() throws IllegalConversionException;

	/**
	 * Get the value as a int variable.
	 * 
	 * @return int representation of the value.
	 * @throws IllegalConversionException
	 *             if the value cannot be converted to a int.
	 */
	public int getIntegerValue() throws IllegalConversionException;

	/**
	 * Get the value as a byte[] object.
	 * 
	 * @return byte[] representation of the value.
	 * @throws IllegalConversionException
	 *             if the value cannot be converted to a byte[].
	 */
	public long getLongValue() throws IllegalConversionException;

	/**
	 * Get the value as a String object.
	 * 
	 * @return String representation of the value.
	 * @throws IllegalConversionException
	 *             if the value cannot be converted to a String.
	 */
	public String getStringValue() throws IllegalConversionException;

	/**
	 * Get the value as a byte[] object.
	 * 
	 * @return byte[] representation of the value.
	 * @throws IllegalConversionException
	 *             if the value cannot be converted to a byte[].
	 */
	public byte[] getByteArrayValue() throws IllegalConversionException;

	/**
	 * Get the value as a boolean object.
	 * 
	 * @return boolean representation of the value.
	 * @throws IllegalConversionException
	 *             if the value cannot be converted to a boolean.
	 */
	public boolean getBooleanValue() throws IllegalConversionException;

	/**
	 * Get the value as a Java object. Relevant for channels delivering complex Java objects.
	 * 
	 * @return Java object from channel
	 */
	public Object getObjectValue();

	/**
	 * Creates a copy of this.
	 */
	public Value clone() throws CloneNotSupportedException;
}
