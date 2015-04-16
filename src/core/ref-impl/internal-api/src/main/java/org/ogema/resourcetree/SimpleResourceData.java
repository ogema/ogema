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
package org.ogema.resourcetree;

/**
 * Each node of a simple resource in resource tree owns an instance of SimpleResourceData that holds the value(s) of the
 * resource. SimpleResourceData instance can be obtained by calling {@link TreeElement.getData()}. Note that the right
 * method to be called depends on the type of the simple resource. A call to a method that doesn't match the type of the
 * simple resource causes an {@link UnsupportedOperationException}. For example if the simple resource has the type
 * BooleanResource the setter/getter getBoolean()/setBoolean() are to be called. Calls to all other getter/setter cause
 * the {@link UnsupportedOperationException}.
 * 
 */
public interface SimpleResourceData {

	// TODO make an enum and change the references to DBConstants in the reference implementation
	static final public int TYPE_KEY_BOOLEAN = 0;
	static final public int TYPE_KEY_FLOAT = 1;
	static final public int TYPE_KEY_INT = 2;
	static final public int TYPE_KEY_LONG = 3;
	static final public int TYPE_KEY_STRING = 4;
	static final public int TYPE_KEY_BOOLEAN_ARR = 5;
	static final public int TYPE_KEY_FLOAT_ARR = 6;
	static final public int TYPE_KEY_INT_ARR = 7;
	static final public int TYPE_KEY_LONG_ARR = 8;
	static final public int TYPE_KEY_STRING_ARR = 9;
	static final public int TYPE_KEY_COMPLEX_ARR = 10;
	static final public int TYPE_KEY_COMPLEX = 11;
	static final public int TYPE_KEY_OPAQUE = 12;

	/**
	 * Gets the boolean value.
	 * 
	 * @return The boolean value.
	 * @throws UnsupportedOperationException
	 *             If the resource is not from type BooleanResource
	 */
	public boolean getBoolean() throws UnsupportedOperationException;

	/**
	 * Sets the boolean value.
	 * 
	 * @param The
	 *            boolean value.
	 * @throws UnsupportedOperationException
	 *             If the resource is not from type BooleanResource
	 */
	public void setBoolean(boolean z) throws UnsupportedOperationException;

	/**
	 * Gets the float value.
	 * 
	 * @return The float value.
	 * @throws UnsupportedOperationException
	 *             If the resource is not from type FloatResource
	 */
	public float getFloat() throws UnsupportedOperationException;

	/**
	 * Sets the Float value.
	 * 
	 * @param The
	 *            Float value.
	 * @throws UnsupportedOperationException
	 *             If the resource is not from type FloatResource
	 */
	public void setFloat(float f) throws UnsupportedOperationException;

	/**
	 * Gets the integer value.
	 * 
	 * @return The integer value.
	 * @throws UnsupportedOperationException
	 *             If the resource is not from type IntegerResource
	 */
	public int getInt() throws UnsupportedOperationException;

	/**
	 * Sets the Integer value.
	 * 
	 * @param The
	 *            Integer value.
	 * @throws UnsupportedOperationException
	 *             If the resource is not from type IntegerResource
	 */
	public void setInt(int i) throws UnsupportedOperationException;

	/**
	 * Gets the Long value.
	 * 
	 * @return The Long value.
	 * @throws UnsupportedOperationException
	 *             If the resource is not from type LongResource
	 */
	public long getLong() throws UnsupportedOperationException;

	/**
	 * Sets the Long value.
	 * 
	 * @param The
	 *            Long value.
	 * @throws UnsupportedOperationException
	 *             If the resource is not from type LongResource
	 */
	public void setLong(long j) throws UnsupportedOperationException;

	/**
	 * Gets the String value.
	 * 
	 * @return The String value.
	 * @throws UnsupportedOperationException
	 *             If the resource is not from type StringResource
	 */
	public String getString() throws UnsupportedOperationException;

	/**
	 * Sets the String value.
	 * 
	 * @param The
	 *            String value.
	 * @throws UnsupportedOperationException
	 *             If the resource is not from type StringResource
	 */
	public void setString(String s) throws UnsupportedOperationException;

	/**
	 * Gets the boolean array value.
	 * 
	 * @return The boolean array value.
	 * @throws UnsupportedOperationException
	 *             If the resource is not from type BooleanArrayResource
	 */
	public boolean[] getBooleanArr() throws UnsupportedOperationException;

	/**
	 * Sets the boolean array value.
	 * 
	 * @param The
	 *            boolean array value.
	 * @throws UnsupportedOperationException
	 *             If the resource is not from type BooleanArrayResource
	 */
	public void setBooleanArr(boolean[] aZ) throws UnsupportedOperationException;

	/**
	 * Gets the Float array value.
	 * 
	 * @return The Float array value.
	 * @throws UnsupportedOperationException
	 *             If the resource is not from type FloatArrayResource
	 */
	public float[] getFloatArr() throws UnsupportedOperationException;

	/**
	 * Sets the Float array value.
	 * 
	 * @param The
	 *            Float array value.
	 * @throws UnsupportedOperationException
	 *             If the resource is not from type FloatArrayResource
	 */
	public void setFloatArr(float[] aF) throws UnsupportedOperationException;

	/**
	 * Gets the Integer array value.
	 * 
	 * @return The Integer array value.
	 * @throws UnsupportedOperationException
	 *             If the resource is not from type IntegerArrayResource
	 */
	public int[] getIntArr() throws UnsupportedOperationException;

	/**
	 * Sets the Integer array value.
	 * 
	 * @param The
	 *            Integer array value.
	 * @throws UnsupportedOperationException
	 *             If the resource is not from type IntegerArrayResource
	 */
	public void setIntArr(int[] aI) throws UnsupportedOperationException;

	/**
	 * Gets the Long array value.
	 * 
	 * @return The Long array value.
	 * @throws UnsupportedOperationException
	 *             If the resource is not from type LongArrayResource
	 */
	public long[] getLongArr() throws UnsupportedOperationException;

	/**
	 * Sets the Long array value.
	 * 
	 * @param The
	 *            Long array value.
	 * @throws UnsupportedOperationException
	 *             If the resource is not from type LongArrayResource
	 */
	public void setLongArr(long[] aJ) throws UnsupportedOperationException;

	/**
	 * Gets the String array value.
	 * 
	 * @return The String array value.
	 * @throws UnsupportedOperationException
	 *             If the resource is not from type StringArrayResource
	 */
	public String[] getStringArr() throws UnsupportedOperationException;

	/**
	 * Sets the String array value.
	 * 
	 * @param The
	 *            String array value.
	 * @throws UnsupportedOperationException
	 *             If the resource is not from type StringArrayResource
	 */
	public void setStringArr(String[] aS) throws UnsupportedOperationException;

	/**
	 * Gets the byte array value.
	 * 
	 * @return The byte array value.
	 * @throws UnsupportedOperationException
	 *             If the resource is not from type OpaqueResource
	 */
	public byte[] getByteArr() throws UnsupportedOperationException;

	/**
	 * Sets the byte array value.
	 * 
	 * @param The
	 *            byte array value.
	 * @throws UnsupportedOperationException
	 *             If the resource is not from type OpaqueResource
	 */
	public void setByteArr(byte[] aB) throws UnsupportedOperationException;

	/**
	 * Gets the length of the underlying array if this resource is an instance of a simple array resource.
	 * 
	 * @return Length of the array or 0 if the array is not yet initialized.
	 * @throws UnsupportedOperationException
	 *             When the resource is not an instance of a simple array resource.
	 */
	public int getArrayLength() throws UnsupportedOperationException;
}
