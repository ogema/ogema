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
	float getFloatValue() throws IllegalConversionException;

	/**
	 * Get the value as a double variable.
	 * 
	 * @return double representation of the value
	 * @throws IllegalConversionException
	 *             if the value cannot be converted to a double.
	 */
	double getDoubleValue() throws IllegalConversionException;

	/**
	 * Get the value as a int variable.
	 * 
	 * @return int representation of the value.
	 * @throws IllegalConversionException
	 *             if the value cannot be converted to a int.
	 */
	int getIntegerValue() throws IllegalConversionException;

	/**
	 * Get the value as a byte[] object.
	 * 
	 * @return byte[] representation of the value.
	 * @throws IllegalConversionException
	 *             if the value cannot be converted to a byte[].
	 */
	long getLongValue() throws IllegalConversionException;

	/**
	 * Get the value as a String object.
	 * 
	 * @return String representation of the value.
	 * @throws IllegalConversionException
	 *             if the value cannot be converted to a String.
	 */
	String getStringValue() throws IllegalConversionException;

	/**
	 * Get the value as a byte[] object.
	 * 
	 * @return byte[] representation of the value.
	 * @throws IllegalConversionException
	 *             if the value cannot be converted to a byte[].
	 */
	byte[] getByteArrayValue() throws IllegalConversionException;

	/**
	 * Get the value as a boolean object.
	 * 
	 * @return boolean representation of the value.
	 * @throws IllegalConversionException
	 *             if the value cannot be converted to a boolean.
	 */
	boolean getBooleanValue() throws IllegalConversionException;

	/**
	 * Gets a time-series valued entry.
	 * @return time series represented by this value.
	 * @throws IllegalConversionException 
	 *         if the value content cannot be converted to a time series (expect this 
	 *         to be the case for most value types).
	 */
	ReadOnlyTimeSeries getTimeSeriesValue() throws IllegalConversionException;

	/**
	 * Get the value as a Java object. Relevant for channels delivering complex Java objects.
	 * 
	 * @return Java object from channel
	 */
	Object getObjectValue();

	/**
	 * Creates a copy of this.
	 */
	Value clone() throws CloneNotSupportedException;
}
