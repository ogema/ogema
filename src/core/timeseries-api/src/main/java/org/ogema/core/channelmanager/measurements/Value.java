/**
 * Copyright 2011-2018 Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
         * @return a deep copy of this Object.
         * @throws java.lang.CloneNotSupportedException 
	 */
	Value clone() throws CloneNotSupportedException;
}
